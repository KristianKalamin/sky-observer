package sky_observer.space;

public class SpaceObject {
    private String satelliteName;
    private String startFlybyTime;
    private String endFlybyTime;
    private double nakedEyeVisibilityMag;

    public SpaceObject() {
    }

    public SpaceObject(String satelliteName, String startFlybyTime, String endFlybyTime, double nakedEyeVisibilityMag) {
        this.satelliteName = satelliteName;
        this.startFlybyTime = startFlybyTime;
        this.endFlybyTime = endFlybyTime;
        this.nakedEyeVisibilityMag = nakedEyeVisibilityMag;
    }

    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    public String getStartFlybyTime() {
        return startFlybyTime;
    }

    public void setStartFlybyTime(String startFlybyTime) {
        this.startFlybyTime = startFlybyTime;
    }

    public String getEndFlybyTime() {
        return endFlybyTime;
    }

    public void setEndFlybyTime(String endFlybyTime) {
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
                ", startFlybyTime=" + (startFlybyTime == null ? "" : startFlybyTime) +
                ", endFlybyTime=" + (endFlybyTime == null ? "" : endFlybyTime) +
                ", nakedEyeVisibilityMag=" + nakedEyeVisibilityMag +
                '}';
    }
}
