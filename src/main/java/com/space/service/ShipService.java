package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.List;
import java.util.Optional;

public interface ShipService {

    List<Ship> getShips(String name,
                        String planet,
                        ShipType shipType,
                        Long after,
                        Long before,
                        Boolean isUsed,
                        Double minSpeed,
                        Double maxSpeed,
                        Integer minCrewSize,
                        Integer maxCrewSize,
                        Double minRating,
                        Double maxRating);

    void sortShipListByOrder(List<Ship> ships, ShipOrder order);
/*
    Ship createShip(String name, String planet, ShipType shipType, Long prodDate, Boolean isUsed, Double speed, Integer crewSize);
 */
    Ship saveShip(Ship ship);
    Optional<Ship> findShipById(Long id);
    Ship updateShip(Ship oldShip, Ship newShip);
    void deleteShip(Ship ship);

    boolean isShipValid(Ship ship);
    void computeRating(Ship ship);
    Double round(Double d);
}
