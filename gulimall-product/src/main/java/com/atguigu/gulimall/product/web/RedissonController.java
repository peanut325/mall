package com.atguigu.gulimall.product.web;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class RedissonController {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 获取去一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");

        // 加锁
        lock.lock();   //阻塞式等待。默认加的锁都是3es时间。

        try {
            // 锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删
            // 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除。
            System.out.println("加锁成功，业务执行时间..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 最后一定要释放锁，解锁代码之前出现异常，解锁没有运行，redissson不会出现死锁
            System.out.println("释放锁" + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }

    @ResponseBody
    @GetMapping("/hello1")
    public String hello1() {
        // 获取去一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");

        lock.lock(10, TimeUnit.SECONDS); // 10秒自动解锁,自动解锁时间一定要大于业务的执行时间。
        // 问题: Lock.lock(10,TimeUnit.SECONDs);在锁时间到了以后，不会自动续期。
        // 1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        // 2、如果我们未指定锁的超时时间，就使用30 * 1o00【LockwatchdogTimeout看门狗的默认时间】;
        // 只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】,每隔10s都会自动调用
        // internaLLockLeaseTime【看门狗时间】/ 3,10s
        try {
            // 锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删
            // 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除。
            System.out.println("加锁成功，业务执行时间..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 最后一定要释放锁，解锁代码之前出现异常，解锁没有运行，redissson不会出现死锁
            System.out.println("释放锁" + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }

    // 写锁就是排他锁
    @ResponseBody
    @GetMapping("/write")
    public String write() {
        // 获取一把读写锁，只要锁的名字一样，就是同一把锁
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        // 改数据读写锁，写数据加写锁
        rLock.lock();
        try {
            System.out.println("加锁成功，业务执行时间..." + Thread.currentThread().getId());
            Thread.sleep(30000);
            s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }

        return "hello";
    }

    // 读锁就是共享锁
    @ResponseBody
    @GetMapping("/read")
    public String read() {
        // 获取一把读写锁，只要锁的名字一样，就是同一把锁
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.readLock();
        // 改数据读写锁，写数据加写锁
        rLock.lock();
        try {
            System.out.println("加锁成功，业务执行时间..." + Thread.currentThread().getId());
            Thread.sleep(30000);
            s = (String) redisTemplate.opsForValue().get("writeValue");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }

        return s;
    }

    // 信号量测试
    @ResponseBody
    @GetMapping("/park")
    public String park() {
        RSemaphore park = redissonClient.getSemaphore("park");
        // park.acquire(); // 获取一个车位的值，占一个车位，如果车位没有值，会一直等待
        boolean b = park.tryAcquire();
        if (b) {
            // 执行业务
        } else {
            return "没有车位";
        }
        return "ok=" + b;
    }

    // 信号量测试
    @ResponseBody
    @GetMapping("/go")
    public String go() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release(); // 释放一个车位，才能让停车获取
        return "success";
    }

    // 闭锁测试(类似于开关门)
    // 需要door锁中的人全部走了才能关门
    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch downLatch = redissonClient.getCountDownLatch("door");
        downLatch.trySetCount(5);   // 设置5个人走了才能关门
        downLatch.await();  // 关门操作，一直等待没有人才会关门
        return "关门成功!!!";
    }

    // 将闭锁减一，到0才会关门
    @ResponseBody
    @GetMapping("/leave")
    public String leave() throws InterruptedException {
        RCountDownLatch downLatch = redissonClient.getCountDownLatch("door");
        downLatch.countDown();
        return "离开成功!!!";
    }
}
