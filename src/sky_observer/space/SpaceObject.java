package sky_observer.space;

import org.orekit.time.AbsoluteDate;

public class SpaceObject {
    private String satelliteName;
    private AbsoluteDate startFlybyTime;
    private AbsoluteDate endFlybyTime;
    private double nakedEyeVisibilityMag;

    public SpaceObject() {
    }


    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    public AbsoluteDate getStartFlybyTime() {
        return startFlybyTime;
    }

    public void setStartFlybyTime(AbsoluteDate startFlybyTime) {
        this.startFlybyTime = startFlybyTime;
    }

    public AbsoluteDate getEndFlybyTime() {
        return endFlybyTime;
    }

    public void setEndFlybyTime(AbsoluteDate endFlybyTime) {
        this.endFlybyTime = endFlybyTime;
    }

    public double getNakedEyeVisibilityMag() {
        return nakedEyeVisibilityMag;
    }

    public void setNakedEyeVisibilityMag(double nakedEyeVisibilityMag) {
        this.nakedEyeVisibilityMag = nakedEyeVisibilityMag;
    }

    @Override
    public String toString() {
        return "sky_observer.space.SpaceObject{" +
                ", satelliteName='" + satelliteName + '\'' +
                ", startFlybyTime=" + (startFlybyTime == null ? "" : startFlybyTime.getDate().toString()) +
                ", endFlybyTime=" + (endFlybyTime == null ? "" : endFlybyTime.getDate().toString()) +
                ", nakedEyeVisibilityMag=" + nakedEyeVisibilityMag +
                '}';
    }
}
