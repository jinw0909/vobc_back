package io.vobc.vobc_back.service.web3;

import io.vobc.vobc_back.exception.WalletAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletSignatureVerifier {

    @Value("${web3.base.rpc-url}")
    private String baseRpcUrl;

    private static final String ERC1271_MAGIC_VALUE = "0x1626ba7e";

    public boolean verify(String message, String signature, String expectedAddress) {
        if (!isValidHexSignature(signature)) {
            throw new WalletAuthException("Invalid signature format");
        }

        byte[] sigBytes = Numeric.hexStringToByteArray(signature);

        if (sigBytes.length == 65) {
            String recoveredAddress = recoverAddress(message, signature, expectedAddress);
            return expectedAddress.equalsIgnoreCase(recoveredAddress);
        }

        return verifySmartWalletSignature(message, signature, expectedAddress);
    }


    private boolean verifySmartWalletSignature(String message, String signature, String walletAddress) {
        try {
            Web3j web3j = Web3j.build(new HttpService(baseRpcUrl));

            byte[] messageHash = Sign.getEthereumMessageHash(
                    message.getBytes(StandardCharsets.UTF_8)
            );

            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);

            Function function = new Function(
                    "isValidSignature",
                    List.of(
                            new Bytes32(messageHash),
                            new DynamicBytes(signatureBytes)
                    ),
                    List.of(new TypeReference<Bytes4>() {})
            );

            String encodedFunction = FunctionEncoder.encode(function);

            Transaction transaction = Transaction.createEthCallTransaction(
                    null,
                    walletAddress,
                    encodedFunction
            );

            String result = web3j.ethCall(
                    transaction,
                    DefaultBlockParameterName.LATEST
            ).send().getValue();

            if (result == null || result.equals("0x")) {
                return false;
            }

            List<org.web3j.abi.datatypes.Type> decoded =
                    FunctionReturnDecoder.decode(result, function.getOutputParameters());

            if (decoded.isEmpty()) {
                return false;
            }

            Bytes4 magicValue = (Bytes4) decoded.get(0);
            String returnedMagicValue = Numeric.toHexString(magicValue.getValue());

            return ERC1271_MAGIC_VALUE.equalsIgnoreCase(returnedMagicValue);
        } catch (Exception e) {
            return false;
        }
    }

    private String recoverAddress(String message, String signature, String expectedAddress) {
        try {
            Sign.SignatureData signatureData = signatureStringToData(signature);

            byte[] messageHash = Sign.getEthereumMessageHash(
                    message.getBytes(StandardCharsets.UTF_8)
            );

            for (int recId = 0; recId < 4; recId++) {
                BigInteger publicKey = Sign.recoverFromSignature(
                        recId,
                        new ECDSASignature(
                                new BigInteger(1, signatureData.getR()),
                                new BigInteger(1, signatureData.getS())
                        ),
                        messageHash
                );

                if (publicKey == null) {
                    continue;
                }

                String recovered = "0x" + Keys.getAddress(publicKey);
                String normalizedRecovered = recovered.toLowerCase();

                if (normalizedRecovered.equals(expectedAddress.toLowerCase())) {
                    return normalizedRecovered;
                }
            }

            throw new IllegalArgumentException("Failed to recover matching address from signature");
        } catch (Exception e) {
            throw new WalletAuthException("Invalid signature format", e);
        }
    }

    private Sign.SignatureData signatureStringToData(String signature) {
        byte[] sigBytes = Numeric.hexStringToByteArray(signature);

        if (sigBytes.length != 65) {
            throw new WalletAuthException("Invalid signature length");
        }

        byte v = sigBytes[64];
        if (v < 27) {
            v += 27;
        }

        byte[] r = Arrays.copyOfRange(sigBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(sigBytes, 32, 64);

        return new Sign.SignatureData(v, r, s);
    }

    private boolean isValidHexSignature(String signature) {
        if (signature == null || !signature.startsWith("0x")) {
            return false;
        }

        String hex = signature.substring(2);

        return !hex.isBlank()
                && hex.length() % 2 == 0
                && hex.matches("^[0-9a-fA-F]+$");
    }
}
