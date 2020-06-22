import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LinkValidatorSync {

    private static HttpClient client;

    public static void main(String args[]) throws IOException {

        client = HttpClient.newHttpClient();

        /* for synchronous request
        Files.lines(Path.of("urls.txt"))
                .map(LinkValidatorSync::validateLink)
                .forEach(System.out::println);
         */

        //firstly we're going to split performing the requests and handling the CompletableFutures
        var futures = Files.lines(Path.of("urls.txt"))    //mapping the requests to variable futures
                .map(LinkValidatorSync::validateLink)
                .collect(Collectors.toList());
        //the generated list of requests should be executed in parallel

        //our program doesn't ends before the completableFutures are asynchronously completed.
        futures.stream()
                .map(CompletableFuture::join)  //done by join method
                .forEach(System.out::println);
        //we end up having with is stream of status strings for all the requests that have been performed.
    }

            /* for synchronous request
    private static String validateLink(String link){
        HttpRequest request =HttpRequest.newBuilder(URI.create(link))
                .GET()
                .build();

        try {
            HttpResponse<Void> response =client.send(request,
                    HttpResponse.BodyHandlers.discarding());
            return responseToString(response);
        }catch (IOException | InterruptedException exception){
            return String.format("%s -> %s",link,false);
        }
    }
    */

    private static CompletableFuture<String> validateLink(String link){
                HttpRequest request =HttpRequest.newBuilder(URI.create(link))
                        .GET()
                        .build();

                return client.sendAsync(request,HttpResponse.BodyHandlers.discarding()) //CompletableFuture wrapped with response
                        .thenApply(LinkValidatorSync::responseToString)//completableFuture coming out from thenApply will be wrapped with string
                        .exceptionally(e ->String.format("%s -> %s",link,false)); //in case something goes wrong
    }
    //thenApply method only defines the action to be taken when CompletableFuture is completed successfully.

    private static String responseToString(HttpResponse<Void> response){
        int status =response.statusCode();
        boolean success =status >=200 && status <=299;
        return String.format("%s -> %s (status :%s)",response.uri(),success,status);
    }
}
