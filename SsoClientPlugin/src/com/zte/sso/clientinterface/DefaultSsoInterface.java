package com.zte.sso.clientinterface;

import java.util.List;

import com.zte.sso.pojo.Organization;
import com.zte.sso.pojo.SsoUser;

/**
 * 如果应用没有实现SsoInterface就用这个默认实现，全部返回失败。
 * 
 * @author ZZC
 *
 */
public class DefaultSsoInterface implements SsoInterface {
	@Override
	public boolean addUsers(List<SsoUser> users) {
		return false;
	}

	@Override
	public boolean removeUsers(List<String> usernames) {
		return false;
	}

	@Override
	public boolean updateUsers(List<String> usernames) {
		return false;
	}

	@Override
	public boolean addOrganizations(List<Organization> organizations) {
		return false;
	}

	@Override
	public boolean removeOrganizations(List<String> departmentNames) {
		return false;
	}
}
