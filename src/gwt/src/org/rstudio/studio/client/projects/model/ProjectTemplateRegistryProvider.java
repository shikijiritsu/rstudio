/*
 * ProjectTemplateRegistryProvider.java
 *
 * Copyright (C) 2009-16 by RStudio, Inc.
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
package org.rstudio.studio.client.projects.model;

import java.util.ArrayList;
import java.util.List;

import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.projects.events.ProjectTemplateRegistryUpdatedEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProjectTemplateRegistryProvider
      implements ProjectTemplateRegistryUpdatedEvent.Handler
{
   public interface Callback
   {
      public void execute(ProjectTemplateRegistry registry);
   }
   
   public ProjectTemplateRegistryProvider()
   {
      RStudioGinjector.INSTANCE.injectMembers(this);
      
      pendingCallbacks_ = new ArrayList<Callback>();
   }
   
   @Inject
   private void initialize(EventBus events)
   {
      events_ = events;
      
      events_.addHandler(ProjectTemplateRegistryUpdatedEvent.TYPE, this);
   }
   
   public ProjectTemplateRegistry getProjectTemplateRegistry()
   {
      return registry_ == null
            ? ProjectTemplateRegistry.createProjectTemplateRegistry()
            : registry_;
   }
   
   public void withProjectTemplateRegistry(Callback callback)
   {
      if (registry_ == null)
         pendingCallbacks_.add(callback);
      else
         callback.execute(registry_);
   }
   
   @Override
   public void onProjectTemplateRegistryUpdated(ProjectTemplateRegistryUpdatedEvent event)
   {
      // update registry
      registry_ = event.getData();
      
      // execute pending callbacks
      for (Callback callback : pendingCallbacks_)
         callback.execute(registry_);
      
      // clear pending callbacks
      pendingCallbacks_.clear();
   }
   
   private ProjectTemplateRegistry registry_;
   private final List<Callback> pendingCallbacks_;
   
   // Injected ----
   private EventBus events_;
}