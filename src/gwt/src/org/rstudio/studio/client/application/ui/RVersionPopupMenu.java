/*
 * RVersionPopupMenu.java
 *
 * Copyright (C) 2020 Ricardo Wurmus
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.application.ui;

import org.rstudio.core.client.ElementIds;
import org.rstudio.core.client.command.AppCommand;
import org.rstudio.core.client.resources.ImageResource2x;
import org.rstudio.core.client.theme.res.ThemeResources;
import org.rstudio.core.client.theme.res.ThemeStyles;
import org.rstudio.core.client.widget.ToolbarButton;
import org.rstudio.core.client.widget.ToolbarMenuButton;
import org.rstudio.core.client.widget.ToolbarPopupMenu;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.application.Desktop;
import org.rstudio.studio.client.application.events.SwitchToRVersionEvent;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.application.model.RVersionSpec;
import org.rstudio.studio.client.application.model.RVersionsInfo;
import org.rstudio.studio.client.server.ServerError;
import org.rstudio.studio.client.server.ServerRequestCallback;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.model.Session;
import org.rstudio.studio.client.workbench.model.SessionInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.inject.Inject;

public class RVersionPopupMenu extends ToolbarPopupMenu
{
   /**
    *
    * @param sessionInfo
    * @param commands
    */
   public RVersionPopupMenu(SessionInfo sessionInfo, Commands commands)
   {
      RStudioGinjector.INSTANCE.injectMembers(this);
      
      commands_ = commands;

      RVersionsInfo rVersionsInfo = sessionInfo.getRVersionsInfo();
      availableVersions_ = rVersionsInfo.getAvailableRVersions();
      rVersion_ = RVersionSpec.create(rVersionsInfo.getRVersion(),
                                      rVersionsInfo.getRVersionHome(),
                                      rVersionsInfo.getRVersionLabel());
   }
   
   @Inject
   void initialize(EventBus events,
                   Session session)
   {
      events_ = events;
   }
   
   public ToolbarButton getToolbarButton()
   {
      String buttonText = "R version";

      if (toolbarButton_ == null)
      {
         toolbarButton_ = new ToolbarMenuButton(
                buttonText,
                ToolbarButton.NoTitle,
                null,
                this, 
                true);
         ElementIds.assignElementId(toolbarButton_, ElementIds.VERSION_MENUBUTTON);
      }

      toolbarButton_.setTitle(buttonText);
      return toolbarButton_;
   }
   
   @Override
   protected ToolbarMenuBar createMenuBar()
   {
      return new RVersionPopupMenuBar();
   }
   
   private class RVersionPopupMenuBar extends ToolbarMenuBar
   {
      public RVersionPopupMenuBar()
      {
         super(true);
      }
   }
   
   @Override
   public void getDynamicPopupMenu(final DynamicPopupMenuCallback callback)
   {
      rebuildMenu(null, callback);
   }

   private void rebuildMenu(final JsArray<RVersionSpec> versions,
         DynamicPopupMenuCallback callback)
   {
      // clean out existing entries
      clearItems();

      // ensure the menu doesn't get too narrow
      addSeparator(225);

      // add as many MRU items as is appropriate for our screen size and number
      // of available versions
      AppCommand[] versionCommands = new AppCommand[] {
         commands_.projectVersion0(),
         commands_.projectVersion1(),
         commands_.projectVersion2(),
         commands_.projectVersion3(),
         commands_.projectVersion4(),
         commands_.projectVersion5(),
         commands_.projectVersion6(),
         commands_.projectVersion7(),
         commands_.projectVersion8(),
         commands_.projectVersion9()
      };
      
      for (int i = 0; i < Math.min(versionCommands.length, MAX_VERSIONS); i++)
      {
         addItem(versionCommands[i].createMenuItem(false));
      }

      for (int i = 0; i < Math.min(availableVersions_.length(),
                                   MAX_VERSIONS); i ++)
      {
          final RVersionSpec version = availableVersions_.get(i);
          String menuHtml = AppCommand.formatMenuLabel(
            null, version.getVersion() + " (" + version.getLabel() + ")", false, null);
            addItem(new MenuItem(menuHtml, true, () ->
            {
                events_.fireEvent(new SwitchToRVersionEvent(version));
            }));
      }
      
      callback.onPopupMenu(this);
   }

   private static final Resources RESOURCES = GWT.create(Resources.class);

   private static final int MAX_VERSIONS = 10;
   private JsArray<RVersionSpec> availableVersions_;
   private final RVersionSpec rVersion_;
   private ToolbarMenuButton toolbarButton_ = null;

   private final Commands commands_;
   private EventBus events_;
}
