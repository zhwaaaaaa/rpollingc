package com.zhw.rpollingc.http;

public class PersonBuilder extends Person {

    public Person build() {
        Person person = new Person();
        person.setAge(getAge());
        person.setName(getName());
        person.setSex(isSex());
        return person;
    }

}
