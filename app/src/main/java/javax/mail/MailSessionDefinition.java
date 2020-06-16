/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.mail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Repeatable;

/**
 * Annotation used by Jakarta EE applications to define a <code>MailSession</code>
 * to be registered with JNDI.  The <code>MailSession</code> may be configured
 * by setting the annotation elements for commonly used <code>Session</code>
 * properties.  Additional standard and vendor-specific properties may be
 * specified using the <code>properties</code> element.
 * <p>
 * The session will be registered under the name specified in the
 * <code>name</code> element.  It may be defined to be in any valid
 * <code>Jakarta EE</code> namespace, and will determine the accessibility of
 * the session from other components.
 *
 * @since JavaMail 1.5
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MailSessionDefinitions.class)
public @interface MailSessionDefinition {

    /**
     * Description of this mail session.
     *
     * @return	the description
     */
    String description() default "";

    /**
     * JNDI name by which the mail session will be registered.
     *
     * @return	the JNDI name
     */
    String name();

    /**
     * Store protocol name.
     *
     * @return	the store protocol name
     */
    String storeProtocol() default "";

    /**
     * Transport protocol name.
     *
     * @return	the transport protocol name
     */
    String transportProtocol() default "";

    /**
     * Host name for the mail server.
     *
     * @return	the host name
     */
    String host() default "";

    /**
     * User name to use for authentication.
     *
     * @return	the user name
     */
    String user() default "";

    /**
     * Password to use for authentication.
     *
     * @return	the password
     */
    String password() default "";

    /**
     * From address for the user.
     *
     * @return	the from address
     */
    String from() default "";

    /**
     * Properties to include in the Session.
     * Properties are specified using the format:
     * <i>propertyName=propertyValue</i> with one property per array element.
     *
     * @return	the properties
     */
    String[] properties() default {};
}
