package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import static com.ezylang.evalex.operators.OperatorIfc.OPERATOR_PRECEDENCE_COMPARISON;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.operators.AbstractOperator;
import com.ezylang.evalex.operators.InfixOperator;
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;

public class ExpressionHelper {
    private static final List<String> EXPR_VARIABLES = Collections.unmodifiableList(Arrays.asList(
            "received", "to", "from", "subject", "text", "hasAttachments"
    ));

    static void check(Expression expression) throws ParseException {
        for (String variable : expression.getUsedVariables()) {
            Log.i("EXPR variable=" + variable);
            if (!EXPR_VARIABLES.contains(variable))
                throw new IllegalArgumentException("Unknown variable '" + variable + "'");
        }
        Log.i("EXPR validating");
        expression.validate();
        Log.i("EXPR validated");
    }

    static Expression getExpression(EntityRule rule, EntityMessage message, List<Header> headers, String html, Context context) throws JSONException, ParseException, MessagingException {
        // https://ezylang.github.io/EvalEx/

        JSONObject jcondition = new JSONObject(rule.condition);
        if (!jcondition.has("expression"))
            return null;
        String eval = jcondition.getString("expression");

        List<String> to = new ArrayList<>();
        if (message != null && message.to != null)
            for (Address a : message.to)
                to.add(MessageHelper.formatAddresses(new Address[]{a}));

        List<String> from = new ArrayList<>();
        if (message != null && message.from != null)
            for (Address a : message.from)
                from.add(MessageHelper.formatAddresses(new Address[]{a}));

        if (html == null && message != null && message.content)
            try {
                html = Helper.readText(message.getFile(context));
            } catch (IOException ex) {
                Log.e(ex);
            }

        Document doc = (html == null ? null : JsoupEx.parse(html));

        if (headers == null && message != null && message.headers != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(message.headers.getBytes());
            headers = Collections.list(new InternetHeaders(bis, true).getAllHeaders());
        }

        HeaderFunction fHeader = new HeaderFunction(headers);
        MessageFunction fMessage = new MessageFunction(message);
        BlocklistFunction fBlocklist = new BlocklistFunction(context, message, headers);
        MxFunction fMx = new MxFunction(context, message);
        AttachmentsFunction fAttachments = new AttachmentsFunction(context, message);
        JsoupFunction fJsoup = new JsoupFunction(context, message);
        SizeFunction fSize = new SizeFunction();
        KnownFunction fKnown = new KnownFunction(context, message);
        AIFunction fAI = new AIFunction(context, message, doc);

        ContainsOperator oContains = new ContainsOperator(false);
        ContainsOperator oMatches = new ContainsOperator(true);

        ExpressionConfiguration configuration = ExpressionConfiguration.defaultConfiguration();

        configuration.getFunctionDictionary().addFunction("Header", fHeader);
        configuration.getFunctionDictionary().addFunction("Message", fMessage);
        configuration.getFunctionDictionary().addFunction("Blocklist", fBlocklist);
        configuration.getFunctionDictionary().addFunction("onBlocklist", fBlocklist);
        configuration.getFunctionDictionary().addFunction("hasMx", fMx);
        configuration.getFunctionDictionary().addFunction("attachments", fAttachments);
        configuration.getFunctionDictionary().addFunction("Jsoup", fJsoup);
        configuration.getFunctionDictionary().addFunction("Size", fSize);
        configuration.getFunctionDictionary().addFunction("knownContact", fKnown);
        configuration.getFunctionDictionary().addFunction("AI", fAI);

        configuration.getOperatorDictionary().addOperator("Contains", oContains);
        configuration.getOperatorDictionary().addOperator("Matches", oMatches);

        Expression expression = new Expression(eval, configuration)
                .with("received", message == null ? null : message.received)
                .with("to", to)
                .with("from", from)
                .with("subject", message == null ? null : message.subject)
                .with("text", doc == null ? null : doc.text());

        if (message != null) {
            boolean hasAttachments = false;
            for (String variable : expression.getUsedVariables())
                if (!hasAttachments && "hasAttachments".equals(variable)) {
                    hasAttachments = true;
                    DB db = DB.getInstance(context);
                    List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                    expression.with("hasAttachments", attachments != null && !attachments.isEmpty());
                }
        }

        return expression;
    }

    static boolean needsHeaders(Expression expression) {
        try {
            expression.validate();
            for (ASTNode node : expression.getAllASTNodes()) {
                Token token = node.getToken();
                Log.i("EXPR token=" + token.getType() + ":" + token.getValue());
                if (token.getType() == Token.TokenType.FUNCTION &&
                        ("header".equalsIgnoreCase(token.getValue()) ||
                                "blocklist".equalsIgnoreCase(token.getValue()))) {
                    Log.i("EXPR needs headers");
                    return true;
                }
            }
        } catch (Throwable ex) {
            Log.e("EXPR", ex);
        }
        return false;
    }

    static boolean needsBody(Expression expression) {
        try {
            for (String variable : expression.getUsedVariables())
                if ("text".equalsIgnoreCase(variable))
                    return true;

            expression.validate();
            for (ASTNode node : expression.getAllASTNodes()) {
                Token token = node.getToken();
                Log.i("EXPR token=" + token.getType() + ":" + token.getValue());
                if (token.getType() == Token.TokenType.FUNCTION &&
                        "AI".equalsIgnoreCase(token.getValue())) {
                    Log.i("EXPR needs body");
                    return true;
                }
            }
        } catch (Throwable ex) {
            Log.e("EXPR", ex);
        }
        return false;
    }

    @FunctionParameter(name = "value")
    public static class HeaderFunction extends AbstractFunction {
        private final List<Header> headers;

        HeaderFunction(List<Header> headers) {
            this.headers = headers;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            List<String> result = new ArrayList<>();

            try {
                if (parameterValues.length == 1) {
                    String name = parameterValues[0].getStringValue();
                    if (name != null && headers != null)
                        for (Header header : headers)
                            if (name.equalsIgnoreCase(header.getName()))
                                result.add(header.getValue());
                }
            } catch (Throwable ex) {
                Log.e("EXPR", ex);
            }

            Log.i("EXPR header(" + parameterValues[0] + ")=" + TextUtils.join(", ", result));
            return EvaluationValue.of(result, ExpressionConfiguration.defaultConfiguration());
        }
    }

    @FunctionParameter(name = "value")
    public static class MessageFunction extends AbstractFunction {
        private final EntityMessage message;

        MessageFunction(EntityMessage message) {
            this.message = message;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            List<Object> result = new ArrayList<>();

            try {
                if (parameterValues.length == 1) {
                    String name = parameterValues[0].getStringValue();
                    if (name != null && message != null) {
                        Field field = message.getClass().getField(name);
                        field.setAccessible(true);
                        Object value = field.get(message);
                        if (value != null)
                            result.add(value);
                    }
                }
            } catch (Throwable ex) {
                Log.e("EXPR", ex);
            }

            Log.i("EXPR message(" + parameterValues[0] + ")=" + TextUtils.join(", ", result));
            return EvaluationValue.of(result, ExpressionConfiguration.defaultConfiguration());
        }
    }

    public static class BlocklistFunction extends AbstractFunction {
        private final Context context;
        private final List<Header> headers;
        private final EntityMessage message;

        BlocklistFunction(Context context, EntityMessage message, List<Header> headers) {
            this.context = context;
            this.message = message;
            this.headers = headers;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            boolean result = false;

            try {
                if (message != null && message.from != null)
                    result = Boolean.TRUE.equals(DnsBlockList.isJunk(context, Arrays.asList(message.from)));

                List<String> received = new ArrayList<>();
                if (headers != null)
                    for (Header header : headers)
                        if (header.getName().equalsIgnoreCase("Received"))
                            received.add(header.getValue());
                result = result || Boolean.TRUE.equals(DnsBlockList.isJunk(context, received.toArray(new String[0])));
            } catch (Throwable ex) {
                Log.e("EXPR", ex);
            }

            Log.i("EXPR blocklist()=" + result);
            return expression.convertValue(result);
        }
    }

    public static class MxFunction extends AbstractFunction {
        private final Context context;
        private final EntityMessage message;

        MxFunction(Context context, EntityMessage message) {
            this.context = context;
            this.message = message;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            boolean result = false;

            try {
                Address[] addresses =
                        (message.reply == null || message.reply.length == 0
                                ? message.from : message.reply);
                DnsHelper.checkMx(context, addresses);
                result = true;
            } catch (Throwable ex) {
                Log.e("EXPR", ex);
            }

            Log.i("EXPR mx()=" + result);
            return expression.convertValue(result);
        }
    }

    @FunctionParameter(name = "value")
    public static class AttachmentsFunction extends AbstractFunction {
        private final Context context;
        private final EntityMessage message;

        AttachmentsFunction(Context context, EntityMessage message) {
            this.context = context;
            this.message = message;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            int result = 0;
            String regex = null;

            if (message != null) {
                DB db = DB.getInstance(context);
                if (parameterValues.length == 1) {
                    regex = parameterValues[0].getStringValue();
                    Pattern p = Pattern.compile(regex);
                    List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                    if (attachments != null)
                        for (EntityAttachment attachment : attachments)
                            if (attachment.name != null && p.matcher(attachment.name).matches())
                                result++;
                } else
                    result = db.attachment().countAttachments(message.id);
            }

            Log.i("EXPR attachments()=" + result);
            return expression.convertValue(result);
        }
    }

    @FunctionParameter(name = "value")
    public static class JsoupFunction extends AbstractFunction {
        private final Context context;
        private final EntityMessage message;

        JsoupFunction(Context context, EntityMessage message) {
            this.context = context;
            this.message = message;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            List<String> result = new ArrayList<>();

            if (message != null && message.content && parameterValues.length == 1)
                try {
                    String query = parameterValues[0].getStringValue();
                    File file = message.getFile(context);
                    Document d = JsoupEx.parse(file);
                    for (Element element : d.select(query))
                        result.add(element.text());
                } catch (Throwable ex) {
                    Log.e("EXPR", ex);
                }

            Log.i("EXPR jsoup(" + parameterValues[0] + ")=" + TextUtils.join(", ", result));
            return EvaluationValue.of(result, ExpressionConfiguration.defaultConfiguration());
        }
    }

    @FunctionParameter(name = "value")
    public static class SizeFunction extends AbstractFunction {
        SizeFunction() {
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            int result = 0;

            if (parameterValues.length == 1 &&
                    parameterValues[0].getDataType() == EvaluationValue.DataType.ARRAY)
                result = parameterValues[0].getArrayValue().size();

            Log.i("EXPR size()=" + result);
            return expression.convertValue(result);
        }
    }

    public static class KnownFunction extends AbstractFunction {
        private final Context context;
        private final EntityMessage message;

        KnownFunction(Context context, EntityMessage message) {
            this.context = context;
            this.message = message;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            boolean result = false;

            if (message != null)
                if (message.avatar != null)
                    result = true;
                else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean suggest_sent = prefs.getBoolean("suggest_sent", true);
                    if (suggest_sent) {
                        DB db = DB.getInstance(context);

                        List<Address> senders = new ArrayList<>();
                        if (message.from != null)
                            senders.addAll(Arrays.asList(message.from));
                        if (message.reply != null)
                            senders.addAll(Arrays.asList(message.reply));
                        for (Address sender : senders) {
                            InternetAddress ia = (InternetAddress) sender;
                            String email = ia.getAddress();

                            EntityContact contact =
                                    db.contact().getContact(message.account, EntityContact.TYPE_TO, email);
                            if (contact != null) {
                                result = true;
                                break;
                            }
                        }
                    }
                }

            Log.i("EXPR known()=" + result);
            return expression.convertValue(result);
        }
    }

    @FunctionParameter(name = "value")
    public static class AIFunction extends AbstractFunction {
        private final Context context;
        private final EntityMessage message;
        private final Document doc;

        AIFunction(Context context, EntityMessage message, Document doc) {
            this.context = context;
            this.message = message;
            this.doc = doc;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token functionToken, EvaluationValue... parameterValues) {
            String result = null;

            try {
                if (doc != null && parameterValues.length == 1) {
                    String prompt = parameterValues[0].getStringValue();
                    if (!TextUtils.isEmpty(prompt)) {
                        result = AI.completeChat(context, -1L, true, doc.text(), null, prompt).toString();
                        EntityLog.log(context, EntityLog.Type.Rules, message, "AI result=" + result);
                    }
                }
            } catch (Throwable ex) {
                Log.w(ex);
            }

            Log.i("EXPR AI()=" + result);
            return expression.convertValue(result);
        }
    }

    @InfixOperator(precedence = OPERATOR_PRECEDENCE_COMPARISON)
    public static class ContainsOperator extends AbstractOperator {
        private final boolean regex;

        ContainsOperator(boolean regex) {
            this.regex = regex;
        }

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token operatorToken, EvaluationValue... operands) {
            boolean result = false;

            try {
                if (operands.length == 2) {
                    List<EvaluationValue> array;
                    if (operands[1].getDataType() == EvaluationValue.DataType.ARRAY)
                        array = operands[0].getArrayValue();
                    else
                        array = Arrays.asList(operands[0]);

                    String condition = operands[1].getStringValue();

                    if (array != null && !array.isEmpty() && !TextUtils.isEmpty(condition))
                        for (EvaluationValue item : array) {
                            String value = item.getStringValue();
                            if (!TextUtils.isEmpty(value))
                                if (regex
                                        ? Pattern.compile(condition, Pattern.DOTALL).matcher(value).matches()
                                        : value.toLowerCase().contains(condition.toLowerCase())) {
                                    result = true;
                                    break;
                                }
                        }
                }
            } catch (Throwable ex) {
                Log.e("EXPR", ex);
            }

            Log.i("EXPR " + operands[0] + (regex ? " MATCHES " : " CONTAINS ") + operands[1] +
                    " regex=" + regex + " result=" + result);

            return expression.convertValue(result);
        }
    }
}
