package org.codewith3h.finmateapplication.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.codewith3h.finmateapplication.EntityResolver;
import org.codewith3h.finmateapplication.config.VNPayConfig;
import org.codewith3h.finmateapplication.dto.request.PremiumPaymentRequest;
import org.codewith3h.finmateapplication.dto.response.PremiumPaymentResponse;
import org.codewith3h.finmateapplication.entity.PremiumPackage;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.entity.UserPremiumPackage;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.mapper.UserPremiumPackageMapper;
import org.codewith3h.finmateapplication.repository.PremiumPackageRepository;
import org.codewith3h.finmateapplication.repository.UserPremiumPackageRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ROLE_USER')")
public class PaymentService {

    UserPremiumPackageRepository userPremiumPackageRepository;
    UserPremiumPackageMapper userPremiumPackageMapper;
    PremiumPackageRepository premiumPackageRepository;
    EntityResolver entityResolver;

    public String createPayment(PremiumPaymentRequest request, HttpServletRequest httpServletRequest) {
        log.info("User [{}] is initiating payment for package [{}]",
                request.getUserId(), request.getPackageId());


        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new AppException(ErrorCode.AMOUNT_MUST_BE_POSITIVE);
        }

        UserPremiumPackage userPremiumPackage = userPremiumPackageMapper.toUserPremiumPackage(request, premiumPackageRepository, entityResolver);

        userPremiumPackageRepository.save(userPremiumPackage);

//        int orderId = orderDAO.addOrder(order);
//        log.info("Created order with ID: {}", orderId);
//
//        if (orderId < 1) {
//            throw new AppException(ErrorCode.ORDER_CREATION_FAILED);
//        }
//
//        for (CartItem item : selectedItems) {
//            OrderDetail orderDetail = new OrderDetail();
//            orderDetail.setOrderId(orderId);
//            orderDetail.setProductId(item.getProductId());
//            orderDetail.setQuantity(item.getQuantity());
//            orderDetail.setUnitPrice(item.getProduct().getPrice());
//
//            if (!orderDetailDAO.addOrderDetail(orderDetail)) {
//                log.error("Failed to add order detail for productId: {}", item.getProductId());
//            }
//        }

        String vnp_TxnRef = userPremiumPackage.getId() + "," + (100000 + new Random().nextInt(900000));
        Map<String, String> vnp_Params = createPaymentParams(request, vnp_TxnRef, httpServletRequest);
        String queryUrl = buildQueryUrl(vnp_Params);
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, buildHashData(vnp_Params));
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;

        log.info("Payment URL generated: {}", paymentUrl);

        return paymentUrl;
    }

    public boolean handlePaymentReturn(PremiumPaymentResponse params, HttpServletRequest httpServletRequest) {
        String vnp_ResponseCode = params.getVnp_ResponseCode();
        String vnp_TxnRef = params.getVnp_TxnRef();
        Integer userPremiumPackageId = Integer.parseInt(vnp_TxnRef.split(",")[0]);
        String status = "00".equals(vnp_ResponseCode) ? "Success" : "Failed";

        log.info("Processing payment return for orderId: {}, status: {}", userPremiumPackageId, status);
        UserPremiumPackage userPremiumPackage = userPremiumPackageRepository.findUserPremiumPackageById(userPremiumPackageId);

        if (status.equals("Success")) {
            // xu ly success
            userPremiumPackage.setIsActive(true);
            userPremiumPackageRepository.save(userPremiumPackage);

            log.info("User premium package [{}] is active for user [{}]. ExpiryDate: [{}]",
                    userPremiumPackage.getPremiumPackage().getName(),
                    userPremiumPackage.getUser().getName(),
                    userPremiumPackage.getExpiryDate());
            return true;
        } else {
            log.info("Payment Failed. User premium package [{}] is still un active for user [{}]",
                    userPremiumPackage.getPremiumPackage().getName(),
                    userPremiumPackage.getUser().getName());
            return false;
        }

    }

    private Map<String, String> createPaymentParams(PremiumPaymentRequest request, String vnp_TxnRef, HttpServletRequest httpServletRequest) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) (request.getAmount() * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", request.getLanguage() != null && !request.getLanguage().isEmpty() ? request.getLanguage() : "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", VNPayConfig.getIpAddress(httpServletRequest));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        return vnp_Params;
    }

    private String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        return hashData.toString();
    }

    private String buildQueryUrl(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                }
            }
        }
        return query.toString();
    }


}
