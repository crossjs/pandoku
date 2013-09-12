package com.whenfully.pandoku.dlx;

public class Data {
	Header column;

	Data up;
	Data down;
	Data left;
	Data right;

	private final Object payload;

	public Data(Object payload) {
		up = this;
		down = this;
		left = this;
		right = this;

		this.payload = payload;
	}

	public Object getPayload() {
		return payload;
	}
}
