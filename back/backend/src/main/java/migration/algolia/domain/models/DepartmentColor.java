package migration.algolia.domain.models;

public class DepartmentColor {

    private String color;

    public DepartmentColor(String department) {
        this.color = getColorByDeparment(department);
    }




    private String getColorByDeparment(String department) {
        switch (department.toLowerCase()) {
            case "salud y medicamentos":
                return "#84D24C";
            case "belleza":
                return "#B14497";
            case "cuidado personal":
                return "#63428F";
            case "cuidado del bebé":
            case "bebé":
                return "#14C5C7";
            case "alimentos y bebidas":
            case "hogar, mascotas y otros":
            case "hogar mascotas y otros":
            case "hogar mascota y otros":
                return "#418FDE";
        }
        return "#418FDE";
    }


    public String getColor() {
        return color;
    }

}
