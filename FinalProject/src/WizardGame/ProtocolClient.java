package WizardGame;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.client.GameConnectionClient;

public class ProtocolClient extends GameConnectionClient{
	private MyGame game;
	private GhostManager ghostManager;
	private UUID id;
	
	public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game) throws IOException {	
        super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
	}
}
