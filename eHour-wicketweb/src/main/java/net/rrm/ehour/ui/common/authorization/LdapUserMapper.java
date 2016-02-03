package net.rrm.ehour.ui.common.authorization;

import net.rrm.ehour.domain.User;
import net.rrm.ehour.user.service.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.Collection;
import java.util.Date;


public class LdapUserMapper extends LdapUserDetailsMapper {
    @Autowired
    private UserService userService;
    private static final Logger logger = Logger.getLogger(LdapUserMapper.class);

    private static String ATTRIBUTE_COUNTRY_CODE = "c";
    private static String ATTRIBUTE_PWD_LAST_SET = "pwdlastset";
    private static String ATTRIBUTE_MAIL = "mail";

    private static long convertFileTimeToUnixTime(long fileTime) {
        return fileTime / 10000000 - 11644473600L;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        String dn = ctx.getNameInNamespace();

        logger.info("Mapping user details from context with DN: " + dn);
        DirContextAdapter dca = (DirContextAdapter) ctx;
        Attributes attributes = dca.getAttributes();

        AuthUser authUser;

        User user = userService.getUser(username);

        if (user == null || !user.isActive()) {
            logger.info("Load user by username for " + username + " but user unknown or inactive");
            throw new UsernameNotFoundException("User unknown");
        } else {
            logger.info("Load user by username for " + username + " " + user.toString());

            // Override by attributes from Active Directory
            NamingEnumeration attributeList = attributes.getIDs();

            while (attributeList.hasMoreElements()) {
                String key = (String) attributeList.nextElement();

                logger.debug("Attribute List: " + key);
                try {
                    for (NamingEnumeration e = attributes.get(key).getAll(); e.hasMore(); ) {
                        Object value = e.next();
                        // Use Country code from Active Directory
                        if (key.equalsIgnoreCase(ATTRIBUTE_COUNTRY_CODE)) {
                            logger.info("Setting country code to " + value.toString() + " for " + username);
                            user.setCountry(value.toString());
                        }
                        else if (key.equalsIgnoreCase(ATTRIBUTE_PWD_LAST_SET)) {
                            long unixPwdLastSetTime = convertFileTimeToUnixTime(Long.parseUnsignedLong(value.toString()));
                            logger.info("Setting password last set to " + unixPwdLastSetTime + " for " + username);
                            user.setLastPasswordChangeTime(new Date(unixPwdLastSetTime * 1000));
                        }
                        else if (key.equalsIgnoreCase(ATTRIBUTE_MAIL)) {
                            logger.info("Setting mail to " + value.toString() + " for " + username);
                            user.setEmail(value.toString());
                        }
                        logger.debug("Value: " + value.toString());
                    }
                }
                catch (NamingException e) {
                    logger.error(e);
                }
            }

            authUser = new AuthUser(user);
        }

        return authUser;

    }
}
