package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;

@Service
public class MpaService {
    private final MpaDbStorage mpaStorage;

    public MpaService(MpaDbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<Mpa> getAll() {
        return mpaStorage.findAll();
    }

    public Mpa getById(int id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id=" + id + " не найден"));
    }
}
