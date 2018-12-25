package com.zte.sso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.zte.sso.clientinterface.DefaultSsoInterface;
import com.zte.sso.clientinterface.SsoInterface;
import com.zte.sso.domain.SsoInterfaceResponse;
import com.zte.sso.pojo.Organization;
import com.zte.sso.pojo.SsoUser;

public class SsoClientService {
	private static final String ADD_FAILED = " add failed";
	private SsoInterface ssoInterfaceImpl;

	private static class InstanceHolder {
		public static final SsoClientService instance = new SsoClientService();
	}

	private SsoClientService() {
	}

	public static SsoClientService getInstance() {
		return InstanceHolder.instance;
	}

	public void doSsoClientAction(HttpServletRequest req, HttpServletResponse resp) {
		String requestURI = req.getRequestURI();
		if (requestURI.indexOf(SSOParams.SSO_LOGOUT) >= 0) {
			removeLogoutToken(req);
		}
		if (req.getMethod() == "POST") {
			if (ssoInterfaceImpl == null) {
				try {
					ssoInterfaceImpl = (SsoInterface) Class.forName(SSOParams.INTERFACE_CLASS_NAME).newInstance();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} finally {
					if (ssoInterfaceImpl == null) {
						ssoInterfaceImpl = new DefaultSsoInterface();
					}
				}
			}
			String ssoRequset = getSsoRequestBody(req);
			boolean interfaceResult = false;
			SsoInterfaceResponse interfaceResponse = new SsoInterfaceResponse();
			if (requestURI.indexOf(SSOParams.SSO_SYNC_USER) >= 0) {
				SsoUser ssoUser = JSON.parseObject(ssoRequset, SsoUser.class);
				List<SsoUser> ssoUsers = new ArrayList<SsoUser>();
				ssoUsers.add(ssoUser);
				interfaceResult = ssoInterfaceImpl.addUsers(ssoUsers);
				interfaceResponse.setMessage(ssoUser.getUserName() + ADD_FAILED);
			}
			if (requestURI.indexOf(SSOParams.SSO_SYNC_ORG) >= 0) {
				Organization organization = JSON.parseObject(ssoRequset, Organization.class);
				List<Organization> organizations = new ArrayList<Organization>();
				organizations.add(organization);
				interfaceResult = ssoInterfaceImpl.addOrganizations(organizations);
				interfaceResponse.setMessage(organization.getDepartmentName() + ADD_FAILED);
			}
			if (requestURI.indexOf(SSOParams.SSO_SYNC_ALL) >= 0) {
				interfaceResult = true;
			}
			interfaceResponse.setSuccess(interfaceResult);
			try {
				resp.getWriter().append(JSON.toJSONString(interfaceResponse));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getSsoRequestBody(HttpServletRequest req) {
		try {
			StringBuilder responseSb = new StringBuilder();
			String readerLine = req.getReader().readLine();
			while (readerLine != null) {
				responseSb.append(req.getReader().readLine());
				readerLine = req.getReader().readLine();
			}
			return responseSb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 在缓存中删除该token,客户端再次访问本应用时，从cookie中带来的token无法在TokenCache中查到<br/>
	 * {@link com.zte.sso.SsoFilter#isLogin(HttpServletRequest,HttpServletResponse)}
	 */
	private void removeLogoutToken(HttpServletRequest req) {
		String token = req.getParameter("Z-ACCESS-TOKEN");
		if (token != null) {
			TokenCache.removeToken(token);
		}
	}
}
