import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private subject: Subject<MessageEvent> | undefined;

  connect(url: string): Subject<MessageEvent> {
    if (!this.subject) {
      const ws = new WebSocket(url);
      const subject = new Subject<MessageEvent>();

      ws.onmessage = (msg) => subject.next(msg);
      ws.onerror = (err) => subject.error(err);
      ws.onclose = () => subject.complete();

      this.subject = subject;
    }

    return this.subject;
  }
}
