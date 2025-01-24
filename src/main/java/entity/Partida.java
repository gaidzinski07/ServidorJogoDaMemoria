package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Partida {
    private List<Peca> pecas = new ArrayList<Peca>();
    private List<Jogador> jogadores = new ArrayList<Jogador>();
    private Jogador jogadorAtual;
    private int quantidadePares;

    public Partida(int quantidadePares, Jogador jogador1, Jogador jogador2) {
        this.quantidadePares = quantidadePares;
        this.pecas = embaralharPecas(quantidadePares);
        this.jogadores.add(jogador1);
        this.jogadores.add(jogador2);
        this.jogadorAtual = jogadores.get(0); // Jogador 1 começa
        System.out.println("Partida iniciada com " + pecas.size());
    }
    
    private boolean podeIniciar() {
    	return jogadores.size() == 2;
    }
    
    private void mudarJogadorDaVez() {
    	int indexAtual = jogadores.indexOf(jogadorAtual);
    	int proximoIndex = indexAtual == 0 ? 1 : 0;
    	jogadorAtual = jogadores.get(proximoIndex);
    }
    
    public boolean realizarJogada(int pos1, int pos2) {
    	boolean result = false;
    	if(pos1 < 0 || pos2 < 0 || pos1 >= pecas.size() || pos2 >= pecas.size() || pos1 == pos2) {
    		return false;
    	}
    	Peca peca1 = pecas.get(pos1);
    	Peca peca2 = pecas.get(pos2);
    	if(peca1.getState() == 0 && peca2.getState() == 0) {
    		if(peca1.getId() == peca2.getId()) {
    			peca1.setState(1);
    			peca2.setState(1);
    			jogadorAtual.pontuar();
    			result = true;
    		}
    		else if(peca1.getId() != peca2.getId()) {
    			result = false;
    		}
    	}
    	System.out.println(jogadorAtual.getNickName() + (result ? " acertou!" : " errou!"));
    	mudarJogadorDaVez();
    	return result;
    }
    
    public boolean checkFimDoJogo() {
    	return pecas.stream().allMatch(p -> p.getState() == 1);
    }

    public Jogador finalizaJogo() {
    	return jogadores.stream().max(Comparator.comparing(Jogador::getPontuacao)).orElse(null);
	}

	// Gera as peças embaralhadas
    private List<Peca> embaralharPecas(int quantidadePares) {
        List<Peca> pecas = new ArrayList<>();
        for (int i = 0; i < quantidadePares; i++) {
            pecas.add(new Peca(i, 0)); // Adiciona par 1
            pecas.add(new Peca(i, 0)); // Adiciona par 2
        }
        Collections.shuffle(pecas);
        return pecas;
    }
    
    public void join(Jogador jogador) {
    	if(jogador == null || jogadores.size() >= 2) {
    		return;
    	}
    	jogadores.add(jogador);
    }
    
    public String getPlacar() {
    	return jogadores.get(0).getNickName() + " " + jogadores.get(0).getPontuacao() + "x" + jogadores.get(1).getPontuacao() + " " + jogadores.get(1).getNickName();
    }
    
    public static void main(String[] args) {
		Jogador j1 = new Jogador("Gaid", 0);
		Jogador j2 = new Jogador("Emile", 0);
		Partida partida = new Partida(5, j1, j2);
		
		if(partida.podeIniciar()) {
			System.out.println("Bem vindo ao jogo da memória! Vez do jogador " + partida.getJogadorAtual().getNickName());
			partida.realizarJogada(0, 2);
			System.out.println("Vez do jogador " + partida.getJogadorAtual().getNickName());
			partida.realizarJogada(1, 9);
		}
		
	}
    
}
