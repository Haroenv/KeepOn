package org.faudroids.keepgoing.auth;

import org.roboguice.shaded.goole.common.base.Objects;

/**
 * One signed in user.
 */
public class Account {

	private final String name, imageUrl;

	public Account(String name, String imageUrl) {
		this.name = name;
		this.imageUrl = imageUrl;
	}

	public String getName() {
		return name;
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
				Objects.equal(imageUrl, account.imageUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, imageUrl);
	}

}
