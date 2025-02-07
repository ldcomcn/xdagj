package io.xdag.net.discovery.discv5;

import io.libp2p.core.PeerId;
import io.libp2p.core.crypto.KEY_TYPE;
import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.PrivKey;
import io.xdag.net.discovery.DiscoveryPeer;
import io.xdag.net.libp2p.peer.DiscoveryPeerConverter;
import io.xdag.net.libp2p.peer.LibP2PNodeId;
import org.apache.tuweni.bytes.Bytes;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

public class DiscoveryPeerTest {
    DiscoveryPeer peer;
    String expectStirng;
    @Before
    public void start() throws UnknownHostException {
        PrivKey privKey = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1).component1();
        peer = new DiscoveryPeer(
                Bytes.wrap(privKey.publicKey().raw()),
                new InetSocketAddress(InetAddress.getByAddress(new byte[] {127, 0, 0, 1}), 10000));
        expectStirng = "/ip4/127.0.0.1/tcp/10000/ipfs/"+ new LibP2PNodeId(PeerId.fromPubKey(privKey.publicKey())).toString();

    }
    @Test
    public void TestDiscoveryPeerToDailNodeId(){
        assertEquals(expectStirng, DiscoveryPeerConverter.discoveryPeerToDailId(peer));
    }
}
