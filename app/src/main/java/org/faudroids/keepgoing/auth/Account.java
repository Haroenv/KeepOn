package org.faudroids.keepgoing.auth;

import org.roboguice.shaded.goole.common.base.Objects;

/**
 * One signed in user.
 */
public class Account {

	private final String name, email, imageUrl;

	public Account(String name, String email, String imageUrl) {
		this.name = name;
		this.email = email;
		this.imageUrl = imageUrl;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Account account = (Account) o;
		return Objects.equal(name, account.name) &&
				Objects.equal(imageUrl, account.imageUrl) &&
				Objects.equal(email, account.email);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, imageUrl, email);
	}

}
