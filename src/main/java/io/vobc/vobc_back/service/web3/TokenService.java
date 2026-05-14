package io.vobc.vobc_back.service.web3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TokenService {
    private static final java.lang.String VOB_CONTRACT = "0xD2AcB5BC4851536d64D8DE36E9bC3aeaBa88dD8A";
    private final Web3j web3j;
    public String getVobBalance(java.lang.String walletAddress) {

        try {

            Function function = new Function(
                    "balanceOf",
                    List.of(new Address(walletAddress)),
                    List.of(new TypeReference<Uint256>() {})
            );

            String encoded = FunctionEncoder.encode(function);

            String response = web3j.ethCall(
                    Transaction.createEthCallTransaction(walletAddress, VOB_CONTRACT, encoded),
                    DefaultBlockParameterName.LATEST
            ).send().getValue();

            List<Type> decoded = FunctionReturnDecoder.decode(
                    response,
                    function.getOutputParameters()
            );

            BigInteger rawBalance = (BigInteger) decoded.get(0).getValue();

            // decimals 가정 18 (필요하면 contract에서 읽어도 됨)
            BigDecimal balance = new BigDecimal(rawBalance).divide(BigDecimal.TEN.pow(18));

            return balance.toPlainString();


        } catch (Exception e) {
            throw new RuntimeException("VOB 조회 실패", e);
        }

    }

    public String getBnbBalance(String walletAddress) {
        try {
            BigInteger weiBalance = web3j.ethGetBalance(
                    walletAddress,
                    org.web3j.protocol.core.DefaultBlockParameterName.LATEST
            ).send().getBalance();

            BigDecimal balance = new BigDecimal(weiBalance)
                    .divide(BigDecimal.TEN.pow(18));

            return balance.toPlainString();

        } catch (Exception e) {
            throw new RuntimeException("BNB 조회 실패", e);
        }
    }
}
