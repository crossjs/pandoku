package com.whenfully.pandoku.dlx;

public class Header extends Data {
	private String name;

	int size;

	public Header(String name) {
		super(null);

		this.name = name;
		size = 0;
	}

	public Header(String name, Object payload) {
		super(payload);

		this.name = name;
		size = 0;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	@Override
	public String toString() {
		return name + " (" + size + ")";
	}
}
