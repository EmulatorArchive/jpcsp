/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.network.proonline;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import jpcsp.network.INetworkAdapter;
import jpcsp.network.adhoc.AdhocMessage;
import jpcsp.network.adhoc.AdhocSocket;
import jpcsp.network.adhoc.MatchingObject;

/**
 * @author gid15
 *
 */
public class ProOnlineMatchingObject extends MatchingObject {
	ProOnlineNetworkAdapter proOnline;

	public ProOnlineMatchingObject(INetworkAdapter networkAdapter) {
		super(networkAdapter);
		proOnline = (ProOnlineNetworkAdapter) networkAdapter;
	}

	@Override
	protected AdhocSocket createSocket() throws UnknownHostException, IOException {
		return new ProOnlineAdhocDatagramSocket(proOnline);
	}

	@Override
	public void create() {
		// Open the UDP port in the router
		proOnline.sceNetPortOpen("UDP", getPort());

		super.create();
	}

	@Override
	protected boolean isForMe(AdhocMessage adhocMessage, int port, InetAddress address) {
		return proOnline.isForMe(adhocMessage, port, address);
	}
}
