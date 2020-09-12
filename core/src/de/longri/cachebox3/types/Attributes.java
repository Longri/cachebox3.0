/*
 * Copyright (C) 2014-2020 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.types;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.gui.skin.styles.AttributesStyle;

import java.util.HashMap;

public enum Attributes {
    Default, // 0
    Dogs, // 1
    Access_or_parking_fee, // 2
    Climbing_gear, // 3
    Boat, // 4
    Scuba_gear, // 5
    Recommended_for_kids, // 6
    Takes_less_than_an_hour, // 7
    Scenic_view, // 8
    Significant_Hike, // 9
    Difficult_climbing, // 10
    May_require_wading, // 11
    May_require_swimming, // 12
    Available_at_all_times, // 13
    Recommended_at_night, // 14
    Available_during_winter, // 15
    Cactus, // 16
    Poison_plants, // 17
    Dangerous_Animals, // 18
    Ticks, // 19
    Abandoned_mines, // 20
    Cliff_falling_rocks, // 21
    Hunting, // 22
    Dangerous_area, //23
    Wheelchair_accessible, //24
    Parking_available, //25
    Public_transportation, //26
    Drinking_water_nearby, //27
    Public_restrooms_nearby, //28
    Telephone_nearby, //29
    Picnic_tables_nearby, //30
    Camping_available, //31
    Bicycles, //32
    Motorcycles, //33
    Quads, //34
    Off_road_vehicles, //35
    Snowmobiles, //36
    Horses, //37
    Campfires, //38
    Thorns, //39
    Stealth_required, //40
    Stroller_accessible, //41
    Needs_maintenance, //42
    Watch_for_livestock, //43
    Flashlight_required, //44
    Lost_And_Found_Tour, //45
    Truck_Driver, //46
    Field_Puzzle, //47
    UV_Light_Required, //48
    Snowshoes, //49
    Cross_Country_Skis, //50
    Special_Tool_Required, //51
    Night_Cache, //52
    Park_and_Grab, //53
    Abandoned_Structure, //54
    Short_hike, //55
    Medium_hike, //56
    Long_Hike, //57
    Fuel_Nearby, //58
    Food_Nearby, //59
    Wireless_Beacon, // 60
    Partnership_Cache, // 61
    Seasonal_Access, // 62
    Tourist_Friendly, // 63
    Tree_Climbing, // 64
    Front_Yard, // 65
    Teamwork_Required, // 66
    GeoTour, // 67
    Challenge,
    Bonus,
    Powertrail,
    Solutionchecker,
    ;

    private static HashMap<Attributes, Integer> attributeLookup;
    private boolean negative = false;

    public static DLong GetAttributeDlong(Attributes attrib) {
        return DLong.shift(GetAttributeID(attrib));
    }

    public static int GetAttributeID(Attributes attrib) {
        if (attributeLookup == null)
            ini();
        return attributeLookup.get(attrib);
    }

    public static Attributes getAttributeEnumByGcComId(int id) {
        switch (id) {
            case 1:
                return Dogs;
            case 2:
                return Access_or_parking_fee;
            case 3:
                return Climbing_gear;
            case 4:
                return Boat;
            case 5:
                return Scuba_gear;
            case 6:
                return Recommended_for_kids;
            case 7:
                return Takes_less_than_an_hour;
            case 8:
                return Scenic_view;
            case 9:
                return Significant_Hike;
            case 10:
                return Difficult_climbing;
            case 11:
                return May_require_wading;
            case 12:
                return May_require_swimming;
            case 13:
                return Available_at_all_times;
            case 14:
                return Recommended_at_night;
            case 15:
                return Available_during_winter;
            case 16:
                return Cactus;
            case 17:
                return Poison_plants;
            case 18:
                return Dangerous_Animals;
            case 19:
                return Ticks;
            case 20:
                return Abandoned_mines;
            case 21:
                return Cliff_falling_rocks;
            case 22:
                return Hunting;
            case 23:
                return Dangerous_area;
            case 24:
                return Wheelchair_accessible;
            case 25:
                return Parking_available;
            case 26:
                return Public_transportation;
            case 27:
                return Drinking_water_nearby;
            case 28:
                return Public_restrooms_nearby;
            case 29:
                return Telephone_nearby;
            case 30:
                return Picnic_tables_nearby;
            case 31:
                return Camping_available;
            case 32:
                return Bicycles;
            case 33:
                return Motorcycles;
            case 34:
                return Quads;
            case 35:
                return Off_road_vehicles;
            case 36:
                return Snowmobiles;
            case 37:
                return Horses;
            case 38:
                return Campfires;
            case 39:
                return Thorns;
            case 40:
                return Stealth_required;
            case 41:
                return Stroller_accessible;
            case 42:
                return Needs_maintenance;
            case 43:
                return Watch_for_livestock;
            case 44:
                return Flashlight_required;
            case 45:
                return Lost_And_Found_Tour;
            case 46:
                return Truck_Driver;
            case 47:
                return Field_Puzzle;
            case 48:
                return UV_Light_Required;
            case 49:
                return Snowshoes;
            case 50:
                return Cross_Country_Skis;
            case 51:
                return Special_Tool_Required;
            case 52:
                return Night_Cache;
            case 53:
                return Park_and_Grab;
            case 54:
                return Abandoned_Structure;
            case 55:
                return Short_hike;
            case 56:
                return Medium_hike;
            case 57:
                return Long_Hike;
            case 58:
                return Fuel_Nearby;
            case 59:
                return Food_Nearby;
            case 60:
                return Wireless_Beacon;
            case 61:
                return Partnership_Cache;
            case 62:
                return Seasonal_Access;
            case 63:
                return Tourist_Friendly;
            case 64:
                return Tree_Climbing;
            case 65:
                return Front_Yard;
            case 66:
                return Teamwork_Required;
            case 67:
                return GeoTour;
            case 68:
                return Default; // nicht bekannt
            case 69:
                return Bonus;
            case 70:
                return Powertrail;
            case 71:
                return Challenge;
            case 72:
                return Solutionchecker;
        }

        return Attributes.Default;
    }

    private static void ini() {
        attributeLookup = new HashMap<Attributes, Integer>();
        attributeLookup.put(Default, 0);
        attributeLookup.put(Dogs, 1);
        attributeLookup.put(Access_or_parking_fee, 2);
        attributeLookup.put(Climbing_gear, 3);
        attributeLookup.put(Boat, 4);
        attributeLookup.put(Scuba_gear, 5);
        attributeLookup.put(Recommended_for_kids, 6);
        attributeLookup.put(Takes_less_than_an_hour, 7);
        attributeLookup.put(Scenic_view, 8);
        attributeLookup.put(Significant_Hike, 9);
        attributeLookup.put(Difficult_climbing, 10);
        attributeLookup.put(May_require_wading, 11);
        attributeLookup.put(May_require_swimming, 12);
        attributeLookup.put(Available_at_all_times, 13);
        attributeLookup.put(Recommended_at_night, 14);
        attributeLookup.put(Available_during_winter, 15);
        attributeLookup.put(Cactus, 16);
        attributeLookup.put(Poison_plants, 17);
        attributeLookup.put(Dangerous_Animals, 18);
        attributeLookup.put(Ticks, 19);
        attributeLookup.put(Abandoned_mines, 20);
        attributeLookup.put(Cliff_falling_rocks, 21);
        attributeLookup.put(Hunting, 22);
        attributeLookup.put(Dangerous_area, 23);
        attributeLookup.put(Wheelchair_accessible, 24);
        attributeLookup.put(Parking_available, 25);
        attributeLookup.put(Public_transportation, 26);
        attributeLookup.put(Drinking_water_nearby, 27);
        attributeLookup.put(Public_restrooms_nearby, 28);
        attributeLookup.put(Telephone_nearby, 29);
        attributeLookup.put(Picnic_tables_nearby, 30);
        attributeLookup.put(Camping_available, 31);
        attributeLookup.put(Bicycles, 32);
        attributeLookup.put(Motorcycles, 33);
        attributeLookup.put(Quads, 34);
        attributeLookup.put(Off_road_vehicles, 35);
        attributeLookup.put(Snowmobiles, 36);
        attributeLookup.put(Horses, 37);
        attributeLookup.put(Campfires, 38);
        attributeLookup.put(Thorns, 39);
        attributeLookup.put(Stealth_required, 40);
        attributeLookup.put(Stroller_accessible, 41);
        attributeLookup.put(Needs_maintenance, 42);
        attributeLookup.put(Watch_for_livestock, 43);
        attributeLookup.put(Flashlight_required, 44);
        attributeLookup.put(Lost_And_Found_Tour, 45);
        attributeLookup.put(Truck_Driver, 46);
        attributeLookup.put(Field_Puzzle, 47);
        attributeLookup.put(UV_Light_Required, 48);
        attributeLookup.put(Snowshoes, 49);
        attributeLookup.put(Cross_Country_Skis, 50);
        attributeLookup.put(Special_Tool_Required, 51);
        attributeLookup.put(Night_Cache, 52);
        attributeLookup.put(Park_and_Grab, 53);
        attributeLookup.put(Abandoned_Structure, 54);
        attributeLookup.put(Short_hike, 55);
        attributeLookup.put(Medium_hike, 56);
        attributeLookup.put(Long_Hike, 57);
        attributeLookup.put(Fuel_Nearby, 58);
        attributeLookup.put(Food_Nearby, 59);
        attributeLookup.put(Wireless_Beacon, 60);
        attributeLookup.put(Partnership_Cache, 61);
        attributeLookup.put(Seasonal_Access, 62);
        attributeLookup.put(Tourist_Friendly, 63);
        attributeLookup.put(Tree_Climbing, 64);
        attributeLookup.put(Front_Yard, 65);
        attributeLookup.put(Teamwork_Required, 66);
        attributeLookup.put(GeoTour, 67);
        // no longer == geocaching.com
        attributeLookup.put(Challenge, 68);
        attributeLookup.put(Bonus, 69);
        attributeLookup.put(Powertrail, 70);
        attributeLookup.put(Solutionchecker, 71);
    }

    public static Array<Attributes> getAttributes(DLong attributesPositive, DLong attributesNegative) {
        if (attributesPositive == null) attributesPositive = new DLong(0, 0);
        if (attributesNegative == null) attributesNegative = new DLong(0, 0);
        Array<Attributes> ret = new Array<>();
        if (attributeLookup == null)
            ini();
        for (Attributes attribute : attributeLookup.keySet()) {
            DLong att = Attributes.GetAttributeDlong(attribute);
            if ((att.BitAndBiggerNull(attributesPositive))) {
                attribute.negative = false;
                ret.add(attribute);
            }
        }
        for (Attributes attribute : attributeLookup.keySet()) {
            DLong att = Attributes.GetAttributeDlong(attribute);
            if ((att.BitAndBiggerNull(attributesNegative))) {
                attribute.negative = true;
                ret.add(attribute);
            }
        }
        return ret;
    }

    public String getImageName() {
        if (attributeLookup == null)
            ini();
        String ret = "att_" + String.valueOf(attributeLookup.get(this));

        if (negative) {
            ret += "_0";
        } else {
            ret += "_1";
        }
        return ret;
    }

    @Override
    public String toString() {
        switch (this) {
            case Abandoned_Structure:
                return "Abandoned Structure";
            case Abandoned_mines:
                return "Abandoned mines";
            case Access_or_parking_fee:
                return "Access or parking fee";
            case Available_at_all_times:
                return "Available at all times";
            case Available_during_winter:
                return "Available during winter";
            case Bicycles:
                break;
            case Boat:
                break;
            case Cactus:
                break;
            case Campfires:
                break;
            case Camping_available:
                return "Camping available";
            case Cliff_falling_rocks:
                return "Cliff falling rocks";
            case Climbing_gear:
                return "Climbing gear";
            case Cross_Country_Skis:
                return "Cross Country Skis";
            case Dangerous_Animals:
                return "Dangerous Animals";
            case Dangerous_area:
                return "Dangerous area";
            case Default:
                break;
            case Difficult_climbing:
                return "Difficult climbing";
            case Dogs:
                break;
            case Drinking_water_nearby:
                return "Drinking water nearby";
            case Field_Puzzle:
                return "Field Puzzle";
            case Flashlight_required:
                return "Flashlight required";
            case Food_Nearby:
                return "Food Nearby";
            case Front_Yard:
                return "Front Yard";
            case Fuel_Nearby:
                return "Fuel Nearby";
            case GeoTour:
                break;
            case Horses:
                break;
            case Hunting:
                break;
            case Long_Hike:
                return "Long Hike";
            case Lost_And_Found_Tour:
                return "Lost And Found Tour";
            case May_require_swimming:
                return "May require swimming";
            case May_require_wading:
                return "May require wading";
            case Medium_hike:
                return "Medium hike";
            case Motorcycles:
                break;
            case Needs_maintenance:
                return "Needs maintenance";
            case Night_Cache:
                return "Night Cache";
            case Off_road_vehicles:
                return "Off road vehicles";
            case Park_and_Grab:
                return "Park and Grab";
            case Parking_available:
                return "Parking available";
            case Partnership_Cache:
                return "Partnership Cache";
            case Picnic_tables_nearby:
                return "Picnic tables nearby";
            case Poison_plants:
                return "Poison plants";
            case Public_restrooms_nearby:
                return "Public restrooms nearby";
            case Public_transportation:
                return "Public transportation";
            case Quads:
                break;
            case Recommended_at_night:
                return "Recommended at night";
            case Recommended_for_kids:
                return "Recommended for kids";
            case Scenic_view:
                return "Scenic view";
            case Scuba_gear:
                return "Scuba gear";
            case Seasonal_Access:
                return "Seasonal Access";
            case Short_hike:
                return "Short hike";
            case Significant_Hike:
                return "Significant Hike";
            case Snowmobiles:
                break;
            case Snowshoes:
                break;
            case Special_Tool_Required:
                return "Special Tool Required";
            case Stealth_required:
                return "Stealth required";
            case Stroller_accessible:
                return "Stroller accessible";
            case Takes_less_than_an_hour:
                return "Takes less than an hour";
            case Teamwork_Required:
                return "Teamwork Required";
            case Telephone_nearby:
                return "Telephone nearby";
            case Thorns:
                break;
            case Ticks:
                break;
            case Tourist_Friendly:
                return "Tourist Friendly";
            case Tree_Climbing:
                return "Tree Climbing";
            case Truck_Driver:
                return "Truck Driver";
            case UV_Light_Required:
                return "UV Light Required";
            case Watch_for_livestock:
                return "Watch for livestock";
            case Wheelchair_accessible:
                return "Wheelchair accessible";
            case Wireless_Beacon:
                return "Wireless Beacon";
            case Challenge:
                return "Challenge cache";
            case Bonus:
                return "Bonus cache";
            case Powertrail:
                return "Power trail";
            case Solutionchecker:
                return "Geocaching.com solution checker";
            default:
                break;

        }

        return super.toString();
    }

    public Drawable getDrawable(AttributesStyle attStyle) {
        if (this.negative) {
            switch (this) {
                case Default:
                    return attStyle.att_Default_0;
                case Dogs:
                    return attStyle.att_Dogs_0;
                case Access_or_parking_fee:
                    return attStyle.att_Access_or_parking_fee_0;
                case Climbing_gear:
                    return attStyle.att_Climbing_gear_0;
                case Boat:
                    return attStyle.att_Boat_0;
                case Scuba_gear:
                    return attStyle.att_Scuba_gear_0;
                case Recommended_for_kids:
                    return attStyle.att_Recommended_for_kids_0;
                case Takes_less_than_an_hour:
                    return attStyle.att_Takes_less_than_an_hour_0;
                case Scenic_view:
                    return attStyle.att_Scenic_view_0;
                case Significant_Hike:
                    return attStyle.att_Significant_Hike_0;
                case Difficult_climbing:
                    return attStyle.att_Difficult_climbing_0;
                case May_require_wading:
                    return attStyle.att_May_require_wading_0;
                case May_require_swimming:
                    return attStyle.att_May_require_swimming_0;
                case Available_at_all_times:
                    return attStyle.att_Available_at_all_times_0;
                case Recommended_at_night:
                    return attStyle.att_Recommended_at_night_0;
                case Available_during_winter:
                    return attStyle.att_Available_during_winter_0;
                case Cactus:
                    return attStyle.att_Cactus_0;
                case Poison_plants:
                    return attStyle.att_Poison_plants_0;
                case Dangerous_Animals:
                    return attStyle.att_Dangerous_Animals_0;
                case Ticks:
                    return attStyle.att_Ticks_0;
                case Abandoned_mines:
                    return attStyle.att_Abandoned_mines_0;
                case Cliff_falling_rocks:
                    return attStyle.att_Cliff_falling_rocks_0;
                case Hunting:
                    return attStyle.att_Hunting_0;
                case Dangerous_area:
                    return attStyle.att_Dangerous_area_0;
                case Wheelchair_accessible:
                    return attStyle.att_Wheelchair_accessible_0;
                case Parking_available:
                    return attStyle.att_Parking_available_0;
                case Public_transportation:
                    return attStyle.att_Public_transportation_0;
                case Drinking_water_nearby:
                    return attStyle.att_Drinking_water_nearby_0;
                case Public_restrooms_nearby:
                    return attStyle.att_Public_restrooms_nearby_0;
                case Telephone_nearby:
                    return attStyle.att_Telephone_nearby_0;
                case Picnic_tables_nearby:
                    return attStyle.att_Picnic_tables_nearby_0;
                case Camping_available:
                    return attStyle.att_Camping_available_0;
                case Bicycles:
                    return attStyle.att_Bicycles_0;
                case Motorcycles:
                    return attStyle.att_Motorcycles_0;
                case Quads:
                    return attStyle.att_Quads_0;
                case Off_road_vehicles:
                    return attStyle.att_Off_road_vehicles_0;
                case Snowmobiles:
                    return attStyle.att_Snowmobiles_0;
                case Horses:
                    return attStyle.att_Horses_0;
                case Campfires:
                    return attStyle.att_Campfires_0;
                case Thorns:
                    return attStyle.att_Thorns_0;
                case Stealth_required:
                    return attStyle.att_Stealth_required_0;
                case Stroller_accessible:
                    return attStyle.att_Stroller_accessible_0;
                case Needs_maintenance:
                    return attStyle.att_Needs_maintenance_0;
                case Watch_for_livestock:
                    return attStyle.att_Watch_for_livestock_0;
                case Flashlight_required:
                    return attStyle.att_Flashlight_required_0;
                case Lost_And_Found_Tour:
                    return attStyle.att_Lost_And_Found_Tour_0;
                case Truck_Driver:
                    return attStyle.att_Truck_Driver_0;
                case Field_Puzzle:
                    return attStyle.att_Field_Puzzle_0;
                case UV_Light_Required:
                    return attStyle.att_UV_Light_Required_0;
                case Snowshoes:
                    return attStyle.att_Snowshoes_0;
                case Cross_Country_Skis:
                    return attStyle.att_Cross_Country_Skis_0;
                case Special_Tool_Required:
                    return attStyle.att_Special_Tool_Required_0;
                case Night_Cache:
                    return attStyle.att_Night_Cache_0;
                case Park_and_Grab:
                    return attStyle.att_Park_and_Grab_0;
                case Abandoned_Structure:
                    return attStyle.att_Abandoned_Structure_0;
                case Short_hike:
                    return attStyle.att_Short_hike_0;
                case Medium_hike:
                    return attStyle.att_Medium_hike_0;
                case Long_Hike:
                    return attStyle.att_Long_Hike_0;
                case Fuel_Nearby:
                    return attStyle.att_Fuel_Nearby_0;
                case Food_Nearby:
                    return attStyle.att_Food_Nearby_0;
                case Wireless_Beacon:
                    return attStyle.att_Wireless_Beacon_0;
                case Partnership_Cache:
                    return attStyle.att_Partnership_Cache_0;
                case Seasonal_Access:
                    return attStyle.att_Seasonal_Access_0;
                case Tourist_Friendly:
                    return attStyle.att_Tourist_Friendly_0;
                case Tree_Climbing:
                    return attStyle.att_Tree_Climbing_0;
                case Front_Yard:
                    return attStyle.att_Front_Yard_0;
                case Teamwork_Required:
                    return attStyle.att_Teamwork_Required_0;
                case GeoTour:
                    return attStyle.att_GeoTour_0;
                case Challenge:
                    return attStyle.att_Challenge_0;
                case Bonus:
                    return attStyle.att_Bonus_0;
                case Powertrail:
                    return attStyle.att_Powertrail_0;
                case Solutionchecker:
                    return attStyle.att_Solutionchecker_0;
            }
        } else {
            switch (this) {
                case Default:
                    return attStyle.att_Default_1;
                case Dogs:
                    return attStyle.att_Dogs_1;
                case Access_or_parking_fee:
                    return attStyle.att_Access_or_parking_fee_1;
                case Climbing_gear:
                    return attStyle.att_Climbing_gear_1;
                case Boat:
                    return attStyle.att_Boat_1;
                case Scuba_gear:
                    return attStyle.att_Scuba_gear_1;
                case Recommended_for_kids:
                    return attStyle.att_Recommended_for_kids_1;
                case Takes_less_than_an_hour:
                    return attStyle.att_Takes_less_than_an_hour_1;
                case Scenic_view:
                    return attStyle.att_Scenic_view_1;
                case Significant_Hike:
                    return attStyle.att_Significant_Hike_1;
                case Difficult_climbing:
                    return attStyle.att_Difficult_climbing_1;
                case May_require_wading:
                    return attStyle.att_May_require_wading_1;
                case May_require_swimming:
                    return attStyle.att_May_require_swimming_1;
                case Available_at_all_times:
                    return attStyle.att_Available_at_all_times_1;
                case Recommended_at_night:
                    return attStyle.att_Recommended_at_night_1;
                case Available_during_winter:
                    return attStyle.att_Available_during_winter_1;
                case Cactus:
                    return attStyle.att_Cactus_1;
                case Poison_plants:
                    return attStyle.att_Poison_plants_1;
                case Dangerous_Animals:
                    return attStyle.att_Dangerous_Animals_1;
                case Ticks:
                    return attStyle.att_Ticks_1;
                case Abandoned_mines:
                    return attStyle.att_Abandoned_mines_1;
                case Cliff_falling_rocks:
                    return attStyle.att_Cliff_falling_rocks_1;
                case Hunting:
                    return attStyle.att_Hunting_1;
                case Dangerous_area:
                    return attStyle.att_Dangerous_area_1;
                case Wheelchair_accessible:
                    return attStyle.att_Wheelchair_accessible_1;
                case Parking_available:
                    return attStyle.att_Parking_available_1;
                case Public_transportation:
                    return attStyle.att_Public_transportation_1;
                case Drinking_water_nearby:
                    return attStyle.att_Drinking_water_nearby_1;
                case Public_restrooms_nearby:
                    return attStyle.att_Public_restrooms_nearby_1;
                case Telephone_nearby:
                    return attStyle.att_Telephone_nearby_1;
                case Picnic_tables_nearby:
                    return attStyle.att_Picnic_tables_nearby_1;
                case Camping_available:
                    return attStyle.att_Camping_available_1;
                case Bicycles:
                    return attStyle.att_Bicycles_1;
                case Motorcycles:
                    return attStyle.att_Motorcycles_1;
                case Quads:
                    return attStyle.att_Quads_1;
                case Off_road_vehicles:
                    return attStyle.att_Off_road_vehicles_1;
                case Snowmobiles:
                    return attStyle.att_Snowmobiles_1;
                case Horses:
                    return attStyle.att_Horses_1;
                case Campfires:
                    return attStyle.att_Campfires_1;
                case Thorns:
                    return attStyle.att_Thorns_1;
                case Stealth_required:
                    return attStyle.att_Stealth_required_1;
                case Stroller_accessible:
                    return attStyle.att_Stroller_accessible_1;
                case Needs_maintenance:
                    return attStyle.att_Needs_maintenance_1;
                case Watch_for_livestock:
                    return attStyle.att_Watch_for_livestock_1;
                case Flashlight_required:
                    return attStyle.att_Flashlight_required_1;
                case Lost_And_Found_Tour:
                    return attStyle.att_Lost_And_Found_Tour_1;
                case Truck_Driver:
                    return attStyle.att_Truck_Driver_1;
                case Field_Puzzle:
                    return attStyle.att_Field_Puzzle_1;
                case UV_Light_Required:
                    return attStyle.att_UV_Light_Required_1;
                case Snowshoes:
                    return attStyle.att_Snowshoes_1;
                case Cross_Country_Skis:
                    return attStyle.att_Cross_Country_Skis_1;
                case Special_Tool_Required:
                    return attStyle.att_Special_Tool_Required_1;
                case Night_Cache:
                    return attStyle.att_Night_Cache_1;
                case Park_and_Grab:
                    return attStyle.att_Park_and_Grab_1;
                case Abandoned_Structure:
                    return attStyle.att_Abandoned_Structure_1;
                case Short_hike:
                    return attStyle.att_Short_hike_1;
                case Medium_hike:
                    return attStyle.att_Medium_hike_1;
                case Long_Hike:
                    return attStyle.att_Long_Hike_1;
                case Fuel_Nearby:
                    return attStyle.att_Fuel_Nearby_1;
                case Food_Nearby:
                    return attStyle.att_Food_Nearby_1;
                case Wireless_Beacon:
                    return attStyle.att_Wireless_Beacon_1;
                case Partnership_Cache:
                    return attStyle.att_Partnership_Cache_1;
                case Seasonal_Access:
                    return attStyle.att_Seasonal_Access_1;
                case Tourist_Friendly:
                    return attStyle.att_Tourist_Friendly_1;
                case Tree_Climbing:
                    return attStyle.att_Tree_Climbing_1;
                case Front_Yard:
                    return attStyle.att_Front_Yard_1;
                case Teamwork_Required:
                    return attStyle.att_Teamwork_Required_1;
                case GeoTour:
                    return attStyle.att_GeoTour_1;
                case Challenge:
                    return attStyle.att_Challenge_1;
                case Bonus:
                    return attStyle.att_Bonus_1;
                case Powertrail:
                    return attStyle.att_Powertrail_1;
                case Solutionchecker:
                    return attStyle.att_Solutionchecker_1;
            }
        }
        return null;
    }

    public Attributes setPositive() {
        this.negative = false;
        return this;
    }

    public Attributes setNegative() {
        this.negative = true;
        return this;
    }

    public boolean isNegative() {
        return negative;
    }
}