package org.codewith3h.finmateapplication.mapper;

import org.codewith3h.finmateapplication.dto.response.WalletResponse;
import org.codewith3h.finmateapplication.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WalletMapper {

    WalletResponse walletToWalletResponse(Wallet wallet);
}
