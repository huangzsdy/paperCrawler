package com.papercrawler.model;

import java.util.Objects;

public class Author {
    private String name;
    private String affiliation;
    private String orcid;

    // 构造函数
    public Author() {}

    public Author(String name) {
        this.name = name;
    }

    public Author(String name, String affiliation) {
        this.name = name;
        this.affiliation = affiliation;
    }

    public Author(String name, String affiliation, String orcid) {
        this.name = name;
        this.affiliation = affiliation;
        this.orcid = orcid;
    }

    // Getter和Setter方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(name, author.name) &&
                Objects.equals(orcid, author.orcid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, orcid);
    }

    @Override
    public String toString() {
        return String.format("Author{name='%s', affiliation='%s'}", name, affiliation);
    }
}