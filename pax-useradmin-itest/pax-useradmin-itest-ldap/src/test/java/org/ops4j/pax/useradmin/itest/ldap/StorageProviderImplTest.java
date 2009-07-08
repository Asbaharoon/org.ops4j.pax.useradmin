package org.ops4j.pax.useradmin.itest.ldap;

import java.util.Collection;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.StorageProvider;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

@RunWith(JUnit4TestRunner.class)
public class StorageProviderImplTest {

    @Configuration
    public static Option[] configure()
    {
        return options(
            // install log service using pax runners profile abstraction (there are more profiles, like DS)
            logProfile(),
            profile("spring"),
            // this is how you set the default log level when using pax logging (logProfile)
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value( "INFO" ),

            // a maven dependency. This must be a bundle already.
            mavenBundle().groupId( "org.ops4j.pax.useradmin" ).artifactId( "pax-useradmin-provider-ldap" ).version( "0.0.1-SNAPSHOT" ),
            mavenBundle().groupId( "org.ops4j.pax.useradmin" ).artifactId( "pax-useradmin-service" ).version( "0.0.1-SNAPSHOT" ),
            
            wrappedBundle(mavenBundle().groupId("cglib").artifactId("cglib-nodep").version("2.1_3")),
            wrappedBundle(mavenBundle().groupId("org.easymock").artifactId("easymock").version("2.4")),
            wrappedBundle(mavenBundle().groupId("org.easymock").artifactId("easymockclassextension").version("2.4"))

            // a maven dependency. OSGi meta data (pacakge exports/imports) are being generated by bnd automatically.
//            wrappedBundle(
//                mavenBundle().groupId( "org.ops4j.base" ).artifactId( "ops4j-base-util" ).version( "0.5.3" )
//            )

        );
    }
    
    @Inject
    BundleContext m_bundleContext = null;
    
    protected StorageProvider getStorageProvider() {
        ServiceReference ref = m_bundleContext.getServiceReference(StorageProvider.class.getName());
        if (null != ref) {
            return (StorageProvider) m_bundleContext.getService(ref);
        }
        Assert.fail("No StorageProvider service available");
        return null;
    }
    
//    protected UserAdminImpl getUserAdmin() {
//        ServiceReference ref = m_bundleContext.getServiceReference(UserAdmin.class.getName());
//        if (null != ref) {
//            return (UserAdminImpl) m_bundleContext.getService(ref);
//        }
//        Assert.fail("No UserAdmin service available");
//        return null;
//    }
    
    @Test
    public void findRoles() {
        StorageProvider spi = getStorageProvider();
        UserAdminFactory factory = EasyMock.createMock(UserAdminFactory.class);
        User user = EasyMock.createMock(User.class);
        
        EasyMock.expect(factory.createUser("jdeveloper", null, null)).andReturn(user);
        
        EasyMock.replay(factory, user);
        
//      try {
//            spi.updated(m_properties);
//        } catch (ConfigurationException e) {
//            Assert.fail("Could not update StorageProviderImpl: " + e.getMessage());
//        }
        
        try {
            Collection<Role> roles = spi.findRoles(factory, "cn=*");
            //
            for (Role role : roles) {
                if ("jdeveloper".equals(role.getName())) {
                    System.out.println("role = " + role);
                    return;
                }
            }
        } catch (StorageException e) {
            Assert.fail("Caught StorageException: " + e.getMessage());
        }
        
        // EasyMock.verify(factory, user);
        
        // Assert.fail("Test user not found");
    }
}
