package com.dhy.nio.context;

import com.dhy.nio.context.handler.coreHandler.*;
import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import lombok.extern.slf4j.Slf4j;


/**
 * @author 大忽悠
 * @create 2022/6/1 20:34
 */
@Slf4j
public class HandlerContext {
    private static final Handler head=new HeadHandler();
    private static final Handler end=new EndHandler();
    private static final HandlerWrapper headWrapper = HandlerWrapper.builder().curHandler(head).prev(null).build();
    private static final HandlerWrapper endWrapper = HandlerWrapper.builder().curHandler(end).next(null).prev(headWrapper).build();

    static {
          headWrapper.setNext(endWrapper);
    }

    /**
     * 调用入站处理器链条
     */
    public void invokeInHandlers(Msg msg, Attr attr){
            headWrapper.invokeInHandler(msg,attr);
    }

    /**
     * 调用出栈处理器链条
     */
    public void invokeOutHandlers(Msg msg, Attr attr){
        headWrapper.invokeOutHandler(msg,attr);
    }


    public void addInHandlers(InHandler inHandler){
        operationVerify(inHandler);
        doAddHandler(inHandler);
    }

    public void addOutHandlers(OutHandler outHandler){
        operationVerify(outHandler);
        doAddHandler(outHandler);
    }

    /**
     * 新元素从尾部插入,确保后添加的handler先被执行
     */
    private void doAddHandler(Handler handler){
        HandlerWrapper handlerWrapper = HandlerWrapper.builder().curHandler(handler).prev(endWrapper.getPrev()).next(endWrapper).build();
        endWrapper.addBefore(handlerWrapper);
    }

    public void removeInHandlers(InHandler inHandler){
        operationVerify(inHandler);
        doRemoveHandlers(headWrapper.getNext(),inHandler);
    }

    public void removeOutHandlers(OutHandler outHandler){
       operationVerify(outHandler);
        doRemoveHandlers(headWrapper.getNext(),outHandler);
    }

    private void doRemoveHandlers(HandlerWrapper curHandlerWrapper,Handler targetHandler){
        if(curHandlerWrapper.getCurHandler() instanceof  EndHandler){
            return;
        }
        if(curHandlerWrapper.getCurHandler().equals(targetHandler)){
            curHandlerWrapper.getPrev().setNext(curHandlerWrapper.getNext());
            curHandlerWrapper=null;
            return;
        }
        doRemoveHandlers(curHandlerWrapper.getNext(),targetHandler);
    }

    private void operationVerify(Handler handler){
        if(handler instanceof  HeadHandler||handler instanceof EndHandler){
            throw new IllegalArgumentException("不能添加或移除头处理和尾处理器");
        }
    }
}
