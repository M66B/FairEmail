package javax.mail.internet;

import javax.mail.Address;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class InternetAddressImpl extends InternetAddress {
    public InternetAddressImpl() {
        super();
    }

    public InternetAddressImpl(String address) throws AddressException {
        super(address);
    }

    public InternetAddressImpl(String address, boolean strict) throws AddressException {
        super(address, strict);
    }

    public InternetAddressImpl(String address, String personal) throws UnsupportedEncodingException {
        super(address, personal);
    }

    public InternetAddressImpl(String address, String personal, String charset) throws UnsupportedEncodingException {
        super(address, personal, charset);
    }

    public InternetAddressImpl(InternetAddress address) throws UnsupportedEncodingException {
        setAddress(address.address);
        setPersonal(address.personal);
    }

    @Override
    public boolean equals(Object a) {
        if (!super.equals(a)) return false;

        InternetAddressImpl address1 = this;
        InternetAddress address2 = (InternetAddress) a; // super.equals checked for a instanceof InternetAddress
        // super.equals already checked this.address for equality
        return Objects.equals(address1.getPersonal(), address2.getPersonal());
    }

    @Override
    public int hashCode() {
        String personal = this.getPersonal();
        int personalHash = 0;
        if (personal != null)
            personalHash = personal.hashCode();
        return super.hashCode() + personalHash*2;
    }
}
