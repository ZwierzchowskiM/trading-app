package com.mzwierzchowski.trading_app.repository;



import com.mzwierzchowski.trading_app.model.TradePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradePositionRepository extends JpaRepository<TradePosition, Long> {

}