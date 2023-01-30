package edu.ensicaen.trashsorting.logic.agent;

import edu.ensicaen.trashsorting.TrashSortingApplication;
import edu.ensicaen.trashsorting.logic.Coordinate;
import edu.ensicaen.trashsorting.logic.Environment;
import edu.ensicaen.trashsorting.logic.EnvironmentEntity;
import edu.ensicaen.trashsorting.logic.Waste;

import java.util.ArrayList;
import java.util.UUID;

/**
 * An agent that separates wastes.
 */
public class WasteSeparationAgent extends EnvironmentEntity {
    protected static final double INFLUENCE_DISTANCE_ = 3;
    protected static final double PROBABILITY_CHANGING_DIRECTION_ = 0.05;

    protected Waste waste_;
    protected double velocityX_;
    protected double velocityY_;
    protected boolean busy_ = false;

    private final String id;


    public WasteSeparationAgent(double posX, double posY) {
        id = UUID.randomUUID().toString();

        posX_ = posX;
        posY_ = posY;
        velocityX_ = Environment.getInstance().generator_.nextDouble() - 0.5;
        velocityY_ = Environment.getInstance().generator_.nextDouble() - 0.5;
        normalize();
    }

    public boolean isCarryingAWaste() {
        return waste_ != null;
    }

    public void updatePosition() {
        posX_ += velocityX_;
        posY_ += velocityY_;
    }

    private void chooseDirection() {
        // randomizing direction + avoiding out of bound
        velocityX_ = Environment.getInstance().generator_.nextDouble() - 0.5;
        if (posX_ + velocityX_ <= 0 || posX_ + velocityX_ >= TrashSortingApplication.width) {
            velocityX_ = -velocityX_;
        }

        velocityY_ = Environment.getInstance().generator_.nextDouble() - 0.5;
        if (posY_ + velocityY_ <= 0 || posY_ + velocityY_ >= TrashSortingApplication.height) {
            velocityY_ = -velocityY_;
        }
    }

    public double getAvgProb(ArrayList<Waste> wastes) {
        if (wastes.size() <= 6)
            return 1;
        double avg = 0;
        for (Waste w: wastes) {
            avg += w.probabilityToTake();
        }
        avg /= wastes.size();
        return avg;
    }

    public void makeActionNotBusy(ArrayList<Waste> wastes) {
        double probTake = getAvgProb(wastes); // only pickup small
        for (Waste w : wastes) {
            if (w.influenceArea() >= w.distanceTo(this) && w.probabilityToTake() >= probTake) {
                busy_ = true;
                waste_ = new Waste(w);
                wastes.remove(w); // destroy waste (carried by agent)
                return;
            }
        }
        return;
    }

    public void makeActionBusy(ArrayList<Waste> wastes) {
        for (Waste w : wastes) {
            if (w.influenceArea() >= w.distanceTo(this) && w.type_ == waste_.type_) {
                for (int i = 0; i < waste_.getSize(); i++) {
                    w.increaseSize();
                }
                busy_ = false;
                waste_ = null;
                return;
            }
        }
        return;
    }

    public void makeAction(ArrayList<Waste> wastes) {
        if (!busy_) {
            makeActionNotBusy(wastes);
            return;
        }
        makeActionBusy(wastes);
        return;
    }

    public void updateDirectionAndDecide(ArrayList<Waste> wastes) {
        chooseDirection();
        makeAction(wastes);
        normalize();
        return;
    }

    protected void normalize() {
        double length;

        length = Math.sqrt(velocityX_ * velocityX_ + velocityY_ * velocityY_);
        velocityX_ /= length;
        velocityY_ /= length;
    }

    @Override
    public String toString() {
        return "WasteSeparationAgent{" +
                "id=" + id +
                ", waste_=" + waste_ +
                ", posX_=" + posX_ +
                ", posY_=" + posY_ +
                '}';
    }
}