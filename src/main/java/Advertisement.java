public class Advertisement {
    private final String title;
    private final String link;
    private final String id;

    public Advertisement(String title, String link, String id) {
        this.title = title;
        this.link = link;
        this.id = id;

    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Advertisement that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


}
