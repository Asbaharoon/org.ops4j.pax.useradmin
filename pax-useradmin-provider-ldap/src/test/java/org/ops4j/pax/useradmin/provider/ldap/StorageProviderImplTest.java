/*
 * Copyright 2009 Matthias Kuespert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.useradmin.provider.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.useradmin.provider.ldap.internal.LdapWrapper;
import org.ops4j.pax.useradmin.provider.ldap.internal.StorageProviderImpl;
import org.ops4j.pax.useradmin.service.spi.StorageException;
import org.ops4j.pax.useradmin.service.spi.UserAdminFactory;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.easymock.classextension.*;

public class StorageProviderImplTest {

    private Dictionary<String, String> m_properties = null;
    
    @Before
    public void setup() {
        m_properties = new Hashtable<String, String>();
        //
        m_properties.put(StorageProviderImpl.PROP_LDAP_SERVER_URL, "ldap://localhost");
        m_properties.put(StorageProviderImpl.PROP_LDAP_SERVER_PORT, "8088");
        m_properties.put(StorageProviderImpl.PROP_LDAP_ROOT_DN, "dc=kuespert-web,dc=de");
        m_properties.put(StorageProviderImpl.PROP_LDAP_ACCESS_USER, "");
        m_properties.put(StorageProviderImpl.PROP_LDAP_ACCESS_PWD, "");
        //
        m_properties.put(StorageProviderImpl.PROP_OBJECTCLASS_USER, StorageProviderImpl.DEFAULT_OBJECTCLASS_USER);
        m_properties.put(StorageProviderImpl.PROP_OBJECTCLASS_GROUP, StorageProviderImpl.DEFAULT_OBJECTCLASS_GROUP);
        m_properties.put(StorageProviderImpl.PROP_IDATTR_USER, StorageProviderImpl.DEFAULT_IDATTR_USER);
        m_properties.put(StorageProviderImpl.PROP_IDATTR_GROUP, StorageProviderImpl.DEFAULT_IDATTR_GROUP);
    }
    
	@Test
	public void findRoles() {
	    LdapWrapper wrapper = EasyMock.createMock(LdapWrapper.class);
        UserAdminFactory factory = EasyMock.createMock(UserAdminFactory.class);
        //
        List<Map<String, String>> mockedRoles = new ArrayList<Map<String,String>>();
        //
        Map<String, String> mockedRole1 = new HashMap<String, String>();
        mockedRole1.put(StorageProviderImpl.ATTR_OBJECTCLASS, StorageProviderImpl.DEFAULT_OBJECTCLASS_USER);
        mockedRole1.put("cn", "Joe Developer");
        mockedRole1.put(StorageProviderImpl.DEFAULT_IDATTR_USER, "jdeveloper");
        mockedRoles.add(mockedRole1);
        try {
            EasyMock.expect(wrapper.searchRoles(StorageProviderImpl.DEFAULT_LDAP_ROOT_DN, "cn=*")).andReturn(mockedRoles);
        } catch (StorageException e) {
            // TODO: ignore or fail?
        }
        User user1 = EasyMock.createMock(User.class);

        EasyMock.expect(factory.createUser("jdeveloper", mockedRole1, null)).andReturn(user1);
        EasyMock.expect(user1.getName()).andReturn("jdeveloper");
        
        EasyMock.replay(wrapper, factory, user1);
        
	    StorageProviderImpl spi = new StorageProviderImpl(wrapper);
	    
//	    try {
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
        
        EasyMock.verify(wrapper, factory, user1);
        
        Assert.fail("Test user not found");
	}
}
