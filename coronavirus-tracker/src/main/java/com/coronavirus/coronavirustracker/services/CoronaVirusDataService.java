package com.coronavirus.coronavirustracker.services;

import com.coronavirus.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    public static String VIRUS_DATA_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    //explanation of annotations
    //PostConstruct -> to execute the method fetchVirusData as soon as program runs
    //Scheduled -> as covid data is getting updated into GitHUb repository everyday basis, our code
    //should also fetch this data via scheduled logic
    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException{

        //Inorder to handle the concurrency issue, I am creating new instance of the class mentioned above LocationStats
        List<LocationStats> newStats = new ArrayList<>();


        //calling a url by using GET HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                                          .uri(URI.create(VIRUS_DATA_URL))
                                          .build();
            HttpResponse<String> httpResponse = client.send(request,
                                           HttpResponse.BodyHandlers.ofString());


            //printing data fetched onto console from the response body of above request call
           // System.out.println(httpResponse.body());



        //Using CSV library to manipulate the data into response body received
        //from GET request
        //Also we need to define a reader -> reader basically converts data from string to reader format
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            /*
            //Getting the records from csv file and printing it on console

            String state = record.get("Province/State");
            System.out.println(state);

             */

            //-------Getting the records from csv file and printing it on UI----------//


            //Setting the variables for the instance created by using 'Setters'
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));
            int latestCases = Integer.parseInt(record.get(record.size()-1));
            int prevDayCases = Integer.parseInt(record.get(record.size()-2));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay(latestCases-prevDayCases);
          //  System.out.println(locationStat);
            newStats.add(locationStat);
        }

        this.allStats=newStats;
    }
}
