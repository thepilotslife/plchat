package plchat;

import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;

public class PlayerData
{
    
    static final Pattern TAGPAT = Pattern.compile("<(.*?)>");
    static final Pattern TOKENPAT = Pattern.compile("§+");

    @Nullable
    static PlayerData forName(@NotNull String name)
    {
        try {
            final String url =
                "/account/"
                + URLEncoder.encode(name, "UTF-8")
                + "/statistics/";
            return parse(HTTPRequest.req(url, null).response);
        } catch (Exception e) {
            Logger.log(e);
            Logger.log("couldn't get player info for " + name);
            return null;
        }
    }
    
    @Nullable
    private static PlayerData parse(@NotNull String response)
    {
        int startidx = response.indexOf(
            "<div id=\"content\" class=\"small-10 columns "
            + "no-padding-left small-full\">"
        ); 

        if (startidx == -1) {
            return null;
        }
        
        response = response.substring(startidx + 70);
        
        startidx = response.indexOf("<h4 class='content-header'>Player Achievements</");
        if (startidx == -1) {
            return null;
        }
        
        response = response.substring(0, startidx);
        response = TAGPAT.matcher(response).replaceAll("§");
        response = TOKENPAT.matcher(response).replaceAll("§");
        //System.out.println(response);
        final String[] parts = response.split("§");
        
        final PlayerData pd = new PlayerData();
        
        int i = -1;
        for (String p : parts) {
            i++;
            
            switch (p) {
            case "Nevada License": pd.nevadaLicense = "Yes".equals(parts[i + 1]); break;
            case "Shamal License": pd.shamalLicense = "Yes".equals(parts[i + 1]); break;
            case "Dodo License": pd.dodoLicense = "Yes".equals(parts[i + 1]); break;
            case "Maverick License":
                pd.maverickLicense = "yes".equals(parts[i + 1]); break;
            case "Shamal Missions:": pd.share("shamal", parts[i + 1]); break;
            case "Rescue Missions:": pd.share("rescue", parts[i + 1]); break;
            case "Cargo Missions:": pd.share("cargo", parts[i + 1]); break;
            case "Helicopter Missions:": pd.share("helicopter", parts[i + 1]); break;
            case "AT400 Missions:": pd.share("at400", parts[i + 1]); break;
            case "Dodo Missions:": pd.share("dodo", parts[i + 1]); break;
            case "Military Missions:": pd.share("military", parts[i + 1]); break;
            case "Cargo Drop:": pd.share("cargo drop", parts[i + 1]); break;
            case "Skimmer Missions:": pd.share("skimmer", parts[i + 1]); break;
            case "Trucking Missions:": pd.share("trucking", parts[i + 1]); break;
            case "Courier Deliverys:": pd.share("courier", parts[i + 1]); break;
            case "Total Missions Completed:": pd.total(parts[i + 1]); break;
            case "Contracted Airline:": pd.airline = parts[i + 1]; break;
            case "Contracted Company:": pd.company = parts[i + 1]; break;
            case "Last Connected": pd.lastConnected = parts[i + 1]; break;
            case "Score:": pd.score = parts[i + 1]; break;
            case "Money:": pd.money(parts[i + 1]); break;
            case "POTD Wins:": pd.potds = parts[i + 1]; break;
            case "Player Houses": pd.houses(parts, i + 1); return pd;
            }
        }
        return pd;
    }
    
    /*
    public static void main(String[] args) throws Exception {
        Main.init();
        forName("robin_be");
    }
    */
    
    String name;
    long parseTime;
    boolean nevadaLicense;
    boolean shamalLicense;
    boolean dodoLicense;
    boolean maverickLicense;
    int shareOne;
    String nameOne;
    int shareTwo;
    String nameTwo;
    int shareThree;
    String nameThree;
    int totalMissions;
    String airline;
    String company;
    String lastConnected;
    String score;
    int money;
    String potds;
    int totalHouseSlots;
    int totalHouseCost;
    int totalCarCost;
    ArrayList<House> houses = new ArrayList<>();
    ArrayList<Car> cars = new ArrayList<>();
    
    static class House {
        String location;
        int slots;
        int cost;
    }
    
    static class Car {
        String name;
        int cost;
    }

    PlayerData()
    {
        this.parseTime = System.currentTimeMillis();
    }
    
    private void money(@NotNull String moneystr)
    {
        this.money = Integer.parseInt(moneystr.substring(1));
    }
    
    private void total(@NotNull String sharestr)
    {
        try {
            String[] parts = sharestr.split(" ");
            this.totalMissions = Integer.parseInt(parts[0]);
        } catch (Exception e) {
            Logger.log(e);
        }
    }
    
    private void share(@NotNull String name, @NotNull String sharestr)
    {
        try {
            String[] parts = sharestr.split(" ");
            String shareperc = parts[4].substring(0, parts[4].length() - 1);
            int share = Integer.parseInt(shareperc);
            if (share > this.shareOne) {
                this.shareThree = this.shareTwo;
                this.nameThree = this.nameTwo;
                this.shareTwo = this.shareOne;
                this.nameTwo = this.nameOne;
                this.shareOne = share;
                this.nameOne = name;
                return;
            }
            if (share > this.shareTwo) {
                this.shareThree = this.shareTwo;
                this.nameThree = this.nameTwo;
                this.shareTwo = share;
                this.nameTwo = name;
            }
            if (share > this.shareThree) {
                this.shareThree = share;
                this.nameThree = name;
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }
    
    private void houses(@NotNull String[] parts, int idx)
    {
        try {
            idx += 6; // skip Owner Location Price Slots Last Visit
            
            while (idx < parts.length) {
                if ("Player Vehicles".equals(parts[idx])) {
                    break;
                }
                final House house = new House();
                house.location = parts[idx + 1];
                final String coststr = parts[idx + 2].substring(1).replace(",", "");
                house.cost = Integer.parseInt(coststr);
                house.slots = Integer.parseInt(parts[idx + 3]);
                this.houses.add(house);
                this.totalHouseCost += house.cost;
                this.totalHouseSlots += house.slots;
                idx += 5;
            }
            
            idx += 6; // skip Vehicle Location Max Speed Price Last Visit
            while (idx < parts.length) {
                if (parts[idx].trim().isEmpty()) {
                    break;
                }
                final Car car = new Car();
                car.name = parts[idx];
                final String coststr = parts[idx + 3].substring(1).replace(",", "");
                car.cost = Integer.parseInt(coststr);
                this.cars.add(car);
                this.totalCarCost += car.cost;
                idx += 5;
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

}
