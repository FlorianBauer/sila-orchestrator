package de.fau.clients.orchestrator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrchTester {

    public static void main(final String[] args) throws InterruptedException {

        final OrchClient client = new OrchClient();
        client.findServer();

        log.info("Termination complete.");
    }
}
