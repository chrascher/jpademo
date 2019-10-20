/*
 * (C) Copyright 22019 CGS IT-Solutions (http://cgs.at/).
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
 *
 */
package at.cgsit.training;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * demo for using jpa and hibernate without any JEE or Servlet Container
 *
 * @Author CGS-IT Solutions @2019
 */
@Entity
@Table(name = "account")
@NamedQueries({
    @NamedQuery(
        name = "UserAccount.findAllUnordered",
        query = "SELECT account FROM UserAccount account"),
    @NamedQuery(
        name = "UserAccount.findAllOrderedByName",
        query = "SELECT account FROM UserAccount account ORDER BY account.username"),
    @NamedQuery(
            name = "UserAccount.findByEmail",
            query = "SELECT account FROM UserAccount account where account.email LIKE :email ORDER BY account.username")
})
public class UserAccount {
	
    @Id
    @Column(name = "user_id", unique = true)
    private int id;	

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password",  nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	
	@Override
	public String toString() {
		return "UserAccount [id=" + id + ", username=" + username + ", password=" + password + ", email=" + email + "]";
	}    
    
    
    

}



