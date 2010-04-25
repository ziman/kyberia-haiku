/*
    Kyberia Haiku - advanced community web application
    Copyright (C) 2010 Robert Hritz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package controllers;

import java.io.File;
import play.mvc.*;
import play.Logger;
import models.*;

// find . -name "*java" -exec cat '{}' ';' | wc -l
// find . -name "*java" -exec grep 'TODO' '{}' ';' -print

// zopar pravidiel:
// - v kazdom POST forme tag #{authenticityToken /}, v controlleri potom
// checkAuthenticity();

@With(Secure.class)
public class Application extends Controller {

    @Before
    static void setConnectedUser() {
        renderArgs.put("reqstart", System.currentTimeMillis());
        if(Security.isConnected()) {
            String uid = session.get(User.USERID);
            renderArgs.put("user",   session.get(User.USERNAME));
            renderArgs.put("userid", uid);
            // Logger.info("ahojky " + uid);
            // UserLocation.saveVisit(uid, "some location");
            // - toto enjak doriesit s bookmark visits a tak
            renderArgs.put("newMail", 
                    MessageThread.getUnreadMailNotif(session.get(User.ID)));
        } else {
            // to este neznamena ze sme uplne mimo, snad
            // moze Secure vytvorit nejaku fake session pre
            // nrephlasenych (?)
        }
        // session.put("view", ViewTemplate.getView(uid,req,..?));
    }

    @After
    static void measureTime()
    {
        Long start    = renderArgs.get("reqstart", Long.class);
        Long duration = System.currentTimeMillis() - start;
        // uhm toto nam nepomoze kedze je to az po uplnom ukonceni
        // renderArgs.put("reqdur", duration.toString());
        Logger.info("Request duration " + duration);
    }

    public static void index() {
        // TODO zmenit
        // collection PAGES, kde budu definovane systemove pages? alebo alebo?
        render();
    }

    public static void viewNode(String id) {
        displayNode(id);
    }

    
    public static void viewUserGroup(Long id) {
        render(id);
    }

    public static void addNode(String id, String content) {
        Haiku h = new Haiku();
        // Logger.info("about to add node:" + id + "," + content + "," +
        // Long.parseLong(session.get(User.USERID)) + "," +
        NodeContent parentNode = NodeContent.load(id);
        Long gid = (parentNode == null) ? null : parentNode.gid;
        Long newId = h.addNode(gid, Controller.params.allSimple(),
                Long.parseLong(session.get(User.USERID)),
                    session.get(User.ID) );
        String nid = null;
        if (id == null) {
            nid = NodeContent.loadByGid(newId).getId();
        } else {
            nid = id;
        }
        displayNode(nid);
    }

    // zle
    public static void delAllNodes() {
        Haiku h = new Haiku();
        h.delAllNodes();
        displayNode("0");
    }


    //
    // akcie
    //
    public static void friend(String uid)
    {
        Logger.info("Add friend :: " + uid);
        User.addFriend(session.get(User.ID), uid);
        showUser(uid);
    }

    public static void ignore(String uid)
    {
        Logger.info("Add ignore :: " + uid);
        User.addIgnore(session.get(User.ID), uid);
        showUser(uid);
    }

    public static void ignoreMail(String uid)
    {
        Logger.info("Add ignoreMail :: " + uid);
        User.addIgnoreMail(session.get(User.ID), uid);
        showUser(uid);
    }

    public static void editNode(String id)
    {
        Logger.info("Show edit node:: " + id);
        NodeContent.load(id).edit(Controller.params.allSimple());
        displayNode(id);
    }

    public static void book(String id)
    {
        Logger.info("Bookmark action:: " + id);
        Bookmark.add(id, session.get(User.ID));
        displayNode(id);
    }

    public static void fook(String id)
    {
        Logger.info("Fook ::" + id);
        User.fook(session.get(User.ID), id);
        displayNode(id);
    }

    public static void k(String id)
    {
        Logger.info("K ::" + id);
        NodeContent nc = NodeContent.load(id);
        if (nc != null)
        {
            Nodelist.giveK(nc, session.get(User.ID));
        }
        displayNode(id);
    }

    public static void mk(String id)
    {
        Logger.info("K ::" + id);
        NodeContent nc = NodeContent.load(id);
        if (nc != null)
        {
            Nodelist.giveMK(nc, session.get(User.ID));
        }
        displayNode(id);
    }

    public static void tag(String id, String tag)
    {
        Logger.info("Tag id ::" + id + " with tag ::" + tag);
        NodeContent nc = NodeContent.load(id);
        if (nc != null)
        {
            Taglist.tagNode(nc,tag,session.get(User.ID));
        }
        displayNode(id);
    }

    // interne, po akcii
    private static void displayNode(String id)
    {
        Haiku h = new Haiku();
        int start = 0;
        int count = 30;
        NodeContent node = NodeContent.load(id);
        if (node.canRead(session.get(User.ID))) {
            Long gid = node.gid;
            // logicky UserVisit save asi patri sem, lebo tu vieme co ideme zobrazit
            UserLocation.saveVisit(User.load(session.get(User.ID)), id);
            renderArgs.put("content", h.viewNode(gid));
            renderArgs.put("thread", h.getThreadedChildren(gid,start,count));
            renderArgs.put("id", id);
        } else {
            renderArgs.put("content", "Sorry pal, no see for you");
        }
        render(ViewTemplate.VIEW_NODE_HTML);
        // podl anode nastavit template
        // String template = Haiku.getTemplate(node,user,session.viewtemplate)
        // render(template,id);
        // alebo skor render
    }

    public static void showEditNode(String id) {
        String uid = session.get(User.ID);
        NodeContent node = NodeContent.load(id);
        if (node != null) {
            // check user rights against the node
            renderArgs.put("id", id);
            renderArgs.put("node", node);
            renderArgs.put("users", User.loadUsers());
            render(ViewTemplate.EDIT_NODE_HTML);
        }
    }

    public static void showNodes() {
        render(ViewTemplate.SHOW_NODES_HTML);
    }

    public static void showBookmarks() {
        renderArgs.put("content",
                Bookmark.viewUserBookmarks(session.get(User.ID)));
        render(ViewTemplate.SHOW_BOOKMARKS_HTML);
    }

    public static void showMail(String thread) {
        String uid = session.get(User.ID);
        renderArgs.put("threads",
                MessageThread.viewUserThreads(uid,session));
        renderArgs.put("users", User.loadUsers());
        if (thread == null)
        {
            thread = session.get("LastThreadId");
        }
        renderArgs.put("mailMessages", Message.getMessages(thread, uid));
        render(ViewTemplate.MAIL_HTML);
    }

    public static void sendMail(String to, String content) {
        //String toId   = User.getIdForName(to);
        String fromId = session.get(User.ID);
        // TODO filter content
        Message.send(fromId, to, content);
        renderArgs.put("users", User.loadUsers());
        renderArgs.put("threads",
                MessageThread.viewUserThreads(session.get(User.ID),session));
        renderArgs.put("mailMessages",
                Message.getMessages(session.get("LastThreadId"), fromId));
        render(ViewTemplate.MAIL_HTML);
    }

    public static void showLastNodes() {
        renderArgs.put("nodes", Nodelist.getLastNodes(30));
        render(ViewTemplate.SHOW_LAST_HTML);
    }

    public static void showFriendsContent() {
        renderArgs.put("nodes",
                Activity.showFriendsContent(session.get(User.ID)));
        render(ViewTemplate.SHOW_LAST_HTML);
    }

    public static void showMe() {
        // linka na current usera
        Haiku h = new Haiku();
        renderArgs.put("nodes",
                Nodelist.getUserNodes(session.get(User.ID),null));
        User me = User.load(session.get(User.ID));
        /* should this be lazily evaluated? */
        renderArgs.put("friends", me.listFriends());
        renderArgs.put("uid", session.get(User.ID));
        render(ViewTemplate.SHOW_ME_HTML);
    }

    public static void showUser(String id) {
        Haiku h = new Haiku();
        // renderArgs.put("user", h.viewUser(id));
        User u = User.load(id);
        if ( u !=null ) {
            renderArgs.put("uid", u.getId());
            // renderArgs.put("user", user);
            // renderArgs.put("content",  h.userNodes(Long.parseLong(u.getGid())));
            renderArgs.put("nodes", Nodelist.getUserNodes(id,null));
            render(ViewTemplate.SHOW_USER_HTML);
        }
        // inak co?
    }

    public static void showK() {
        renderArgs.put("nodes", Nodelist.getKlist(100));
        render(ViewTemplate.SHOW_K_HTML);
    }

    public static void showUsers() {
        // list all users
        // re.append(u.id).append(u.getGid()).append(u.username).append("<br>");
        // renderArgs.put("content", User.listUsers());
        renderArgs.put("users", User.loadUsers());
        render(ViewTemplate.SHOW_USERS_HTML);
    }

    public static void showLive() {
        // list all online users and their current/last activities
        renderArgs.put("locations", UserLocation.getAll());
        render(ViewTemplate.SHOW_LIVE_HTML);
    }

    // Set notif. messages
    private static void setMessages()
    {
        // List messages = Cache.get(session.get(User.ID) + "-messages", List.class);
        /*
        if(messages == null) {
            // Cache miss
            messages = Message.findByUser(session.get("user"));
            Cache.set(session.getId() + "-messages", messages, "30mn");
        }
         */
        renderArgs.put("messages", Alert.pop(session.get(User.ID)));
    }

    // Template processing:
    public static void viewNodeT(NodeTemplate template)
    {
        for (TemplateDataDef d : template.getWantedData())
        {
            switch (d.getDatasetName()) {
                    case MAIL:
                        break;
                        // renderArgs.put('mail',Mail.getMail(d.prop1, d.prop2, ...);
                    //  ...
                    // tuto treba zohladnit strankovanie, tj niektore veci pojdu z TemplateDataDef,
                    // niektore ine z requestu / session
            }
        }
    }

    // TODO cesta k obrazkom do konfigu + kontrolovat co sa upladuje
    public static void uploadIcon(String uid, File icon) {
        checkAuthenticity();
        
        String fname = "/code/haiku/public/images/upload/" +
                session.get(User.ID);
        icon.renameTo(new File(fname));
        showMe();
    }
}