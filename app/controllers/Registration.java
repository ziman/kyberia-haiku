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

import models.Haiku;
import models.User;
import models.ViewTemplate;
import play.Logger;
import play.mvc.*;

public class Registration extends Controller {

    public static void addUser(String username, String password) {
        checkAuthenticity();
        Haiku h = new Haiku();
        String userid = User.addUser(Controller.params.allSimple());
        Logger.info("new user:: " + username + "," + userid);
        if (userid != null ) {
            // TODO zobrazime userinfo ale este nie je prihlaseny
            User u = User.load(userid);
            renderArgs.put("uid", u.getIdString());
            renderArgs.put("user",u);
            render(ViewTemplate.SHOW_ME_HTML);
        } else {
            // show registration errors
        }
    }

    // TODO tu by sme mali kontrolovat ci nie je prihlaseny
    public static void showAddUser() {
        // if !Security.isConnected()
        render(ViewTemplate.ADD_USER_HTML);
    }

}
