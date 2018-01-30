/*
 * Copyright @ 2018 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.xmpp.mucclient;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;

import java.util.HashSet;
import java.util.Set;

/**
 * @author bbaldino
 */
public class MucClient
{
    private AbstractXMPPConnection xmppConnection;
    private MultiUserChatManager mucManager;
    private Set<MultiUserChat> joinedMucs = new HashSet<>();

    public MucClient(XMPPTCPConnectionConfiguration config)
        throws Exception {
        xmppConnection = new XMPPTCPConnection(config);
        xmppConnection.connect().login();
        mucManager = MultiUserChatManager.getInstanceFor(xmppConnection);

        // muc is used just to announce 'existence' and for sending presence (about status)
        // jicofo will care about presence messages sent to the muc, but other pieces will only announce things to
        //  the muc (via presence) not consume anything coming from the muc.  anything they consume will come as a
        //  direct message
        // should be able to join/handle multiple mucs (when sending a message, always send to all mucs, when receiving
        // a message, it will already say which muc it's from)
    }

    public void addParticipantListener(PresenceListener presenceListener)
    {
        //TODO: see note below in #createOrJoinMuc about supporting multiple
        // mucs when adding a participant listener
        for (MultiUserChat muc : joinedMucs)
        {
            muc.addParticipantListener(presenceListener);
        }
    }

    public void addIqListener(StanzaListener stanzaListener)
    {
        xmppConnection.addSyncStanzaListener(stanzaListener, new StanzaFilter()
        {
            @Override
            public boolean accept(Stanza stanza)
            {
                return stanza instanceof IQ;
            }
        });
    }

    //TODO: take in entire IQ, or take in the elements and build the IQ here?
    public boolean sendIq(IQ iq)
    {
        try
        {
            xmppConnection.sendStanza(iq);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean createOrJoinMuc(EntityBareJid mucJid, Resourcepart nickname)
    {
        MultiUserChat muc = mucManager.getMultiUserChat(mucJid);
        try
        {
            muc.createOrJoin(nickname);
            joinedMucs.add(muc);
            //TODO: if we join a muc after adding the listener, then we won't get events for it.
            // technically the only use case we have for adding a presence listener is jicofo
            // which will only join a single muc.  should we enforce that?  or make it work this
            // way and save the listener(s) so we can add them to the new muc here?
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    //TODO: take in a pre-made presence packet? or just take in the
    // body and create the presence packet in this method?
    public boolean sendPresence(Presence presence)
    {
        try
        {
            xmppConnection.sendStanza(presence);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
