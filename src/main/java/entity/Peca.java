package entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Peca {
	private int id;
	private int state; //0 = fechada; 1 = aberta
	
	@Override
	public String toString() {
		return id + "";
	}
	
}
