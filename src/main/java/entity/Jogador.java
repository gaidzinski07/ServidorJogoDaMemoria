package entity;

import java.io.PrintWriter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Jogador {

	@NonNull
	private String nickName;
	@NonNull
	private int pontuacao;
	private PrintWriter printer;
	
	public void pontuar() {
		pontuacao += 1;
	}
	
}
