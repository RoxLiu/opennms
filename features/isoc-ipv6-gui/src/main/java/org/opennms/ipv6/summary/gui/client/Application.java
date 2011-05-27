package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint, LocationUpdateEventHandler, HostUpdateEventHandler {
    
    
    public class UpdateGraphCallback implements RequestCallback {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                updateTimelineChart(ChartUtils.convertJSONToDataTable(response.getText()));
            }

        }

        @Override
        public void onError(Request request, Throwable exception) {
            Window.alert("Error Occured updating graph, try refreshing");
        }

    }

    private FlowPanel m_flowPanel;
    AnnotatedTimeLine m_timeline;
    ChartService m_chartService;
    private Navigation m_nav;
    
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
      m_chartService = new DefaultChartService();
      
      FlowPanel navHolder = new FlowPanel();
      navHolder.getElement().getStyle().setFloat(Float.LEFT);
      
      m_nav = new Navigation();
      m_nav.addLocationUpdateEventHandler(this);
      m_nav.addHostUpdateEventHandler(this);
      navHolder.add(m_nav);
      
      m_flowPanel = new FlowPanel();
      m_flowPanel.add(navHolder);
      
      
      Runnable timelineCallback = new Runnable() {

        public void run() {
            
            m_chartService.getAllLocationsAvailability(new RequestCallback() {

                @Override
                public void onResponseReceived(Request request,Response response) {
                    if(response.getStatusCode() == 200) {
                        m_timeline = new AnnotatedTimeLine(ChartUtils.convertJSONToDataTable(response.getText()), createTimelineOptions(), "800px", "350px");
                        
                        m_flowPanel.add(m_timeline);
                        RootPanel.get().add(m_flowPanel);
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error Initializing Chart");
                    
                }});
            
            
        }
          
      };
      
      VisualizationUtils.loadVisualizationApi(timelineCallback, AnnotatedTimeLine.PACKAGE);
      initializeNav();
  }
  
  private void initializeNav() {
    m_chartService.getAllLocations(new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                m_nav.loadLocations(ChartUtils.convertJSONToLocationList(response.getText()));
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            Window.alert("An error occured loading the locations");
        }
        
    });
    
    m_chartService.getAllParticipants(new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                m_nav.loadHosts(ChartUtils.convertJSONToParticipants(response.getText()));
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            Window.alert("An error occured loading participants");
        }});
    
  }

  protected AnnotatedTimeLine.Options createTimelineOptions() {
      AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
      options.setDisplayAnnotations(true);
      options.setDisplayZoomButtons(false);
      options.setLegendPosition(AnnotatedTimeLine.AnnotatedLegendPosition.SAME_ROW);
      return options;
  }

  

  public void onHostUpdate(HostUpdateEvent event) {
      m_chartService.getAvailabilityByParticipant(event.getHost(), new UpdateGraphCallback());
  }

  protected void updateTimelineChart(DataTable dataTable) {
      m_timeline.draw(dataTable, createTimelineOptions());
  }

  public void onLocationUpdate(LocationUpdateEvent event) {
      m_chartService.getAvailabilityByLocation(event.getLocation(), new UpdateGraphCallback());
  }
  
}
