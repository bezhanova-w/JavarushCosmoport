package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional
public class ShipServiceImpl implements ShipService{

    @Autowired
    private ShipRepository shipRepository;

    public List<Ship> getShips(String name,
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
                               Double maxRating) {
        List<Ship> ships = new ArrayList<Ship>();

        Calendar calendar = Calendar.getInstance();
        if (after != null) {
            calendar.setTimeInMillis(after);
        }
        final Integer yearAfter = after == null ? null : calendar.get(Calendar.YEAR);
        if (before != null) {
            calendar.setTimeInMillis(before);
        }
        final Integer yearBefore = before == null ? null : calendar.get(Calendar.YEAR);

        shipRepository.findAll().forEach((ship -> {
            if (name != null && !ship.getName().contains(name)) return;
            if (planet != null && !ship.getPlanet().contains(planet)) return;
            if (shipType != null && shipType != ship.getShipType()) return;

            calendar.setTime(ship.getProdDate());
            if (yearAfter != null && yearAfter.compareTo(calendar.get(Calendar.YEAR)) > 0) return;
            if (yearBefore != null && yearBefore.compareTo(calendar.get(Calendar.YEAR)) <= 0) return;

            if (isUsed != null && !isUsed.equals(ship.getUsed())) return;
            if (minSpeed != null && minSpeed.compareTo(ship.getSpeed()) > 0) return;
            if (maxSpeed != null && maxSpeed.compareTo(ship.getSpeed()) < 0) return;
            if (minCrewSize != null && minCrewSize.compareTo(ship.getCrewSize()) > 0) return;
            if (maxCrewSize != null && maxCrewSize.compareTo(ship.getCrewSize()) < 0) return;
            if (minRating != null && minRating.compareTo(ship.getRating()) > 0) return;
            if (maxRating != null && maxRating.compareTo(ship.getRating()) < 0) return;

            ships.add(ship);
        }));
        return ships;
    }

    @Override
    public void sortShipListByOrder(List<Ship> ships, ShipOrder order) {
        if (order == null) return;

        ships.sort((ship1, ship2) -> {
            switch (order) {
                case ID:        return ship1.getId().compareTo(ship2.getId());
                case SPEED:     return ship1.getSpeed().compareTo(ship2.getSpeed());
                case DATE:      return ship1.getProdDate().compareTo(ship2.getProdDate());
                case RATING:    return ship1.getRating().compareTo(ship2.getRating());
                default:        return 0;
            }
        });
    }

    @Override
    public Ship saveShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public Optional<Ship> findShipById(Long id) {
        return shipRepository.findById(id);
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {

        boolean needToRecomputeRating = false;

        String newName = newShip.getName();
        if (newName != null) {
            if (!isNameValid(newName)) throw new IllegalArgumentException();
            oldShip.setName(newName);
        }

        String newPlanet = newShip.getPlanet();
        if (newPlanet != null) {
            if (!isPlanetValid(newPlanet)) throw new IllegalArgumentException();
            oldShip.setPlanet(newPlanet);
        }

        ShipType newShipType = newShip.getShipType();
        if (newShipType != null) {
            oldShip.setShipType(newShipType);
        }

        Date newProdDate = newShip.getProdDate();
        if (newProdDate != null) {
            if (!isProdDateValid(newProdDate)) throw new IllegalArgumentException();
            oldShip.setProdDate(newProdDate);
            needToRecomputeRating = true;
        }

        Boolean newIsUsed = newShip.getUsed();
        if (newIsUsed != null) {
            oldShip.setUsed(newIsUsed);
            needToRecomputeRating = true;
        }

        Double newSpeed = newShip.getSpeed();
        if (newSpeed != null) {
            newSpeed = round(newSpeed);
            if (!isSpeedValid(newSpeed)) throw new IllegalArgumentException();
            oldShip.setSpeed(newSpeed);
            needToRecomputeRating = true;
        }

        Integer newCrewSize = newShip.getCrewSize();
        if (newCrewSize != null) {
            if (!isCrewSizeValid(newCrewSize)) throw new IllegalArgumentException();
            oldShip.setCrewSize(newCrewSize);
        }

        if (needToRecomputeRating)
            computeRating(oldShip);

        return saveShip(oldShip);
    }

    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);
    }

    @Override
    public boolean isShipValid(Ship ship) {

        String name = ship.getName();
        if (name == null || !isNameValid(name)) return false;

        String planet = ship.getPlanet();
        if (planet == null || !isPlanetValid(planet)) return false;

        if (ship.getShipType() == null) return false;

        if (ship.getSpeed() == null || !isSpeedValid(ship.getSpeed())) return false;

        if (ship.getCrewSize() == null || !isCrewSizeValid(ship.getCrewSize())) return false;

        if (ship.getProdDate() == null || !isProdDateValid(ship.getProdDate())) return false;

        return true;
    }

    private boolean isNameValid(String name) {
        return !name.isEmpty() && name.length() <= 50;
    }

    private boolean isPlanetValid(String planet) {
        return !planet.isEmpty() && planet.length() <= 50;
    }

    private boolean isSpeedValid(Double speed) {
        return speed.compareTo(0.01) >= 0 && speed.compareTo(0.99) <= 0;
    }

    private boolean isCrewSizeValid(Integer crewSize) {
        return crewSize >= 1 && crewSize <= 9999;
    }

    private boolean isProdDateValid(Date prodDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prodDate);
        int yearShipProdTime = calendar.get(Calendar.YEAR);
        return yearShipProdTime >= 2800 && yearShipProdTime <= 3019;
    }

    @Override
    public Double round (Double d) {
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public void computeRating(Ship ship) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        int yearShipProdTime = calendar.get(Calendar.YEAR);
        int yearCurrent = 3019;

        double rating = 80 * ship.getSpeed() * (ship.getUsed() ? 0.5 : 1);
        rating /= (yearCurrent - yearShipProdTime + 1);
        rating = round(rating);

        ship.setRating(rating);
    }
}
