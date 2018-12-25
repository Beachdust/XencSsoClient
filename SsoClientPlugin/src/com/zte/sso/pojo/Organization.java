package com.zte.sso.pojo;

import java.util.List;

/**
 * 用户组织。
 * 
 * @author ZZC
 *
 */
public class Organization {
	private String parentName;
	private String departmentName;
	private String departmentDesc;
	private List<Organization> children;
	private List<SsoUser> users;

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getDepartmentDesc() {
		return departmentDesc;
	}

	public void setDepartmentDesc(String departmentDesc) {
		this.departmentDesc = departmentDesc;
	}

	public List<Organization> getChildren() {
		return children;
	}

	public void setChildren(List<Organization> children) {
		this.children = children;
	}

	public List<SsoUser> getUsers() {
		return users;
	}

	public void setUsers(List<SsoUser> users) {
		this.users = users;
	}
}
