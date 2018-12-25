package com.zte.sso.clientinterface;

import java.util.List;

import com.zte.sso.pojo.Organization;
import com.zte.sso.pojo.SsoUser;

/**
 * 如果有新增接口，可以在新版本的插件中再加一个interface让第三方应用去实现。
 * 
 * @author ZZC
 *
 */
public interface SsoInterface {
	boolean addUsers(List<SsoUser> users);

	boolean removeUsers(List<String> usernames);

	boolean updateUsers(List<String> usernames);

	boolean addOrganizations(List<Organization> organizations);

	boolean removeOrganizations(List<String> departmentNames);
}
