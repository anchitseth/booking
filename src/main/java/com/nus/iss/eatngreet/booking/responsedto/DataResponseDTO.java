package com.nus.iss.eatngreet.booking.responsedto;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class DataResponseDTO extends CommonResponseDto {

	private Map data;

	public Map getData() {
		return data;
	}

	public void setData(Map data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataResponseDTO other = (DataResponseDTO) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else {
			if (!data.equals(other.data))
				return false;
		}
		return true;
	}

}
