package com.microsoft.office365.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.microsoft.office365.DiscoveryInformation;
import com.microsoft.office365.OfficeClient;
import com.microsoft.office365.demo.operations.TapTask;
import com.microsoft.office365.files.FileClient;
import com.microsoft.office365.files.FileSystemItem;


public class DiscoveryFragment extends Fragment {

    private DemoApplication mApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_discovery_list, container, false);
        mApplication = (DemoApplication) getActivity().getApplication();
        
        rootView.findViewById(R.id.startDiscovery).setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                startDiscovery();
            }
        });

        return rootView;
    }

    protected void startDiscovery() {
        final ListView listView = (ListView)this.getActivity().findViewById(R.id.listDiscovery);
        
        new TapTask<List<FileSystemItem>>(this.getActivity(), new Callable<List<FileSystemItem>>() {

            @Override
            public List<FileSystemItem> call() throws Exception {
            	
            	//we get an instance of the office client
            	//look the implementation, because here is where we get the token from ADAL library
                OfficeClient officeClient = mApplication.getOfficeClient(DiscoveryFragment.this.getActivity(), Constants.DISCOVERY_RESOURCE_ID).get();
                
                //we get the discovery information from the endpoint.
                List<DiscoveryInformation> services = officeClient.getDiscoveryInfo().get();
                
                DiscoveryInformation fileService = null;
                for (DiscoveryInformation service : services) {
                	//we look for the MyFiles service capability
                    if (service.getCapability().equals(Constants.MYFILES_CAPABILITY)) {
                        fileService = service;
                        break;
                    }
                }
                
                if (fileService == null) {
                    return null;
                }
                
                //once we have the service, we ask for the resource id.
                String sharepointResourceId = fileService.getServiceResourceId();
                //gets the service endpoint
                String endpointUrl = fileService.getServiceEndpointUri();
                
                //we store the sharepoint url splitting when we find the "_api" component
                String sharepointUrl = endpointUrl.split("_api")[0];
                
                //creates an instance of the FileClient class. This is the File API entry point.
                FileClient fileClient = mApplication.getFileClient(DiscoveryFragment.this.getActivity(), sharepointResourceId, sharepointUrl).get();
                
                return fileClient.getFileSystemItems().get();
            }
        }) {
            @Override
            public void processResult(List<FileSystemItem> files) {
                ArrayList<String> values = new ArrayList<String>();
                
                for (FileSystemItem file : files) {
                    values.add(file.getName());
                }
                
                ListAdapter adapter = new ListAdapter(DiscoveryFragment.this.getActivity(),
                        android.R.layout.simple_list_item_1, values);
                listView.setAdapter(adapter);
            }                    
        }.execute();
    }
}
