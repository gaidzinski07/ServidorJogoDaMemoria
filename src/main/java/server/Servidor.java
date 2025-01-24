package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.naming.CannotProceedException;

import entity.Jogador;
import entity.Partida;

public class Servidor {
    private static final int PORTA = 9876; // Porta do servidor
    private static final String CHAT_PREFIX = "/chat";
    private static final String PLAY_PREFIX = "/play";
    private static final String PLACAR_PREFIX = "/placar";
    private static final String RESULTADO_PREFIX = "/resultado";
    private static List<Jogador> clientes = new ArrayList<>(); // Lista de clientes conectados
    private static Partida partidaAtual;

    public static void main(String[] args) {
        System.out.println("Servidor iniciado na porta " + PORTA);
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + socketCliente.getInetAddress());

                // Cria uma nova thread para o cliente
                Thread threadCliente = new Thread(new GerenciadorCliente(socketCliente));
                threadCliente.start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    // Classe interna para gerenciar cada cliente
    private static class GerenciadorCliente implements Runnable {
        private Socket socket;
        private PrintWriter escritor;
        private BufferedReader leitor;
        private Jogador jogador = new Jogador();

        public GerenciadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
            	if(clientes.size() >= 2) {
            		throw new CannotProceedException("Servidor já está cheio. Tente novamente mais tarde!");
            	}
                // Configura o leitor e escritor para o cliente
                leitor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                escritor = new PrintWriter(socket.getOutputStream(), true);
                jogador = new Jogador("Jogador" + clientes.size(), 0, escritor);
                
                // Solicita e recebe o nickname
                escritor.println("Digite seu nickname:");
                jogador.setNickName(leitor.readLine());
                System.out.println(jogador.getNickName() + " entrou no chat.");

                // Informa a todos os clientes que um novo usuário entrou
                broadcast(jogador.getNickName() + " entrou no chat.");

                // Adiciona o cliente à lista
                synchronized (clientes) {
                    clientes.add(jogador);
                    if(clientes.size() == 2) {
                    	partidaAtual = new Partida(5, clientes.get(0), clientes.get(1));
                    }
                }

                String mensagem;
                while ((mensagem = leitor.readLine()) != null) {
                	// Checa tipo da mensagem recebida:
                	// /chat = mensagem de chat; /play = mensagem de jogada.
                    if(mensagem.startsWith(CHAT_PREFIX)) {
                    	
                    	mensagem = mensagem.replaceFirst(CHAT_PREFIX, "");
                    	System.out.println("["+ jogador.getNickName() +"]: " + mensagem);
                    	broadcast(CHAT_PREFIX + "[" + jogador.getNickName() + "]: " + mensagem);  
                    	
                    }else if(mensagem.startsWith(PLAY_PREFIX)){
                    	mensagem = mensagem.replaceFirst(PLAY_PREFIX, "");
                    	if(partidaAtual.getJogadorAtual().equals(jogador)) {
                    		String[] temp = mensagem.split(" ");
                    		int[] list = new int[2];
                    		if(temp.length == 2) {
                    			for(int i = 0; i < temp.length; i++) {
                    				list[i] = Integer.parseInt(temp[i]);
                    			}
                    			//Monta mensagem para os jogadores 
                    			//PADRAO DO RETORNO: /play posicaoPeca IdPeca posicaoPeca IdPeca resultado;
                    			if(partidaAtual.getPecas().get(list[0]).getState() != 1 || partidaAtual.getPecas().get(list[1]).getState() != 1) {
                    				boolean acertou = partidaAtual.realizarJogada(list[0], list[1]);
                    				String retorno = "";
                    				retorno += PLAY_PREFIX + partidaAtual.getPecas().get(list[0]).toString() + " " + list[0] + " ";
                    				retorno += partidaAtual.getPecas().get(list[1]).toString() + " " + list[1];
                    				broadcast(retorno);
                    				broadcast(PLACAR_PREFIX + partidaAtual.getPlacar());
                    				//CHECA VITORIA
                    				if(partidaAtual.checkFimDoJogo()) {
                    					broadcast(partidaAtual.finalizaJogo());
                    				}
                    			}else {
                    				broadcast(CHAT_PREFIX + "[SERVIDOR]: " + jogador.getNickName() + " TENTE UMA JOGADA VALIDA PELO AMOR DE DEUS");
                    			}
                    		}else {
                    			broadcast(CHAT_PREFIX + "[SERVIDOR]: " + jogador.getNickName() + " TENTE UMA JOGADA VALIDA PELO AMOR DE DEUS");
                    		}
                    	}else {
                    		broadcast(CHAT_PREFIX + "[SERVIDOR]: " + jogador.getNickName() + " ESPERE A SUA VEZ");
                    	}
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro com o cliente: " + e.getMessage());
            } catch(CannotProceedException e) {
            	System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            }
            finally {
                // Remove o cliente ao desconectar
                synchronized (clientes) {
                    clientes.remove(jogador);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar o socket: " + e.getMessage());
                }
                System.out.println("Cliente desconectado: " + socket.getInetAddress());
            }
        }

        // Método para enviar mensagem a todos os clientes
        private void broadcast(String mensagem) {
            synchronized (clientes) {
                for (Jogador cliente : clientes) {
                    cliente.getPrinter().println(mensagem); // Envia a mensagem para o cliente
                }
            }
        }
        
        // Método para enviar mensagem a todos os clientes
        private void broadcast(Jogador jogador) {
            synchronized (clientes) {
                for (Jogador cliente : clientes) {
                	cliente.getPrinter().println(RESULTADO_PREFIX + (jogador == cliente ? "Vitória!!!" : "Derrota...")); 
                }
            }
        }
        
    }
}
