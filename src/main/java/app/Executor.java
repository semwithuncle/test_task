package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Executor {
    private TreeMap<Integer, Integer> bidStorage = new TreeMap<>(Comparator.reverseOrder());

    private TreeMap<Integer, Integer> askStorage = new TreeMap<>();
    List<StringBuilder> response = new ArrayList<>();

    public void run() {
        readRequest();
        writeResponseToFile(parse(response));
    }

    private void execute(String request) {
        String[] requestArguments = request.split(",");
        switch (requestArguments[0]) {
            case "u" -> {
                if (requestArguments[3].equals("bid")) {
                    handleUpdate(bidStorage, requestArguments);
                }
                if (requestArguments[3].equals("ask")) {
                    handleUpdate(askStorage, requestArguments);
                }
            }
            case "o" -> {
                if (requestArguments[1].equals("buy")) {
                    handleOrder(askStorage, requestArguments);
                }
                if (requestArguments[1].equals("sell")) {
                    handleOrder(bidStorage, requestArguments);
                }
            }
            case "q" -> {
                if (requestArguments[1].equals("best_ask")) {
                    response.add(handleQuery(askStorage, requestArguments));
                }
                if (requestArguments[1].equals("best_bid")) {
                    response.add(handleQuery(bidStorage, requestArguments));
                }
                if (requestArguments[1].equals("size")) {
                    response.add(handleQuery(bidStorage, requestArguments));
                }
            }
        }
    }

    private String parse(List<StringBuilder> response) {
        return response.parallelStream().collect(Collectors.joining(System.lineSeparator()));
    }

    private void handleOrder(TreeMap<Integer, Integer> map, String[] params) {
        Integer size = Integer.valueOf(params[2]);
        while (map.firstEntry().getValue() <= size) {
            size -= map.firstEntry().getValue();
            map.remove(map.firstKey());
        }
        map.put(map.firstKey(), map.firstEntry().getValue() - size);
    }

    private StringBuilder handleQuery(TreeMap<Integer, Integer> map, String[] params) {
        if (params.length == 2) {
            Integer price = map.firstKey();
            return new StringBuilder()
                    .append(price)
                    .append(",")
                    .append(map.get(price));
        } else {
            int price = Integer.valueOf(params[2]);
            return new StringBuilder().append((askStorage.getOrDefault(price, 0)
                    + bidStorage.getOrDefault(price, 0)));
        }
    }

    private void handleUpdate(TreeMap<Integer, Integer> map, String[] params) {
        Integer price = Integer.valueOf(params[1]);
        Integer count = Integer.valueOf(params[2]);
        if (count == 0) {
            map.remove(price);
            return;
        }
        map.put(price, count);
    }

    private void readRequest() {
        try (BufferedReader reader = new BufferedReader(new FileReader("input.txt"))) {
            String line = reader.readLine();
            while (line != null) {
                execute(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeResponseToFile(String response) {
        try {
            Files.write(Path.of("output.txt"), response.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
