package io.vobc.vobc_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3Config {

    private static final String INFURA_API_KEY = "";
    private static final String BSC_RPC = "https://bsc-dataseed.binance.org/";

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(BSC_RPC));
    }
}
