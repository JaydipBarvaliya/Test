BatchDocResponse response =
        webClient.post()
                .uri("")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BatchDocResponse.class)
                .block();